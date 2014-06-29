package net.coasterman10.rangers;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class ArenaManager {
    private File arenaListFile;
    private Plugin plugin;
    private World world;
    private Map<String, Collection<Arena>> arenas = new HashMap<>();
    private Map<String, GameMap> maps;

    public ArenaManager(Plugin plugin, World world, Map<String, GameMap> maps) {
        this.plugin = plugin;
        this.world = world;
        this.maps = maps; // The game maps know their schematics, so we need to get those by name, hence this map
        
        arenaListFile = new File(plugin.getDataFolder(), "built-arenas.yml");
    }

    // This config isn't meant to be touched by human hands. There are no failsafes for now.
    // If you mess with the arena list file and it breaks, it's your fault.
    @SuppressWarnings("unchecked")
    public void loadArenas() {
        FileConfiguration arenaListConfig = YamlConfiguration.loadConfiguration(arenaListFile);
        List<Map<String, Object>> arenaList = (List<Map<String, Object>>) arenaListConfig.getList("arenas");
        for (Map<String, Object> map : arenaList) {
            String mapName = (String) map.get("map");
            Map<String, Object> lobby = (Map<String, Object>) map.get("lobby");
            Map<String, Object> arena = (Map<String, Object>) map.get("arena");
            Location lobbyLocation = Vector.deserialize(lobby).toLocation(world);
            Location arenaLocation = Vector.deserialize(arena).toLocation(world);
            
            if (!arenas.containsKey(mapName)) {
                arenas.put(mapName, new HashSet<Arena>());
            }
            arenas.get(mapName).add(new Arena(mapName, lobbyLocation, arenaLocation));
        }
    }

    public Arena getArena(String mapName) {
        if (arenas.containsKey(mapName)) {
            // Some arenas are already, built, let's check them
            for (Arena a : arenas.get(mapName)) {
                if (!a.isUsed()) {
                    // We found one that's not being used.
                    return a;
                }
            }
        }
        
        // No arenas of the desired map are available, so we need to build a new one.
        Location lobby = new Vector(0, 64, 0).toLocation(world);
        for (Collection<Arena> collection : arenas.values()) {
            for (Arena a : collection) {
                if (a.getLobbyLocation().getX() > lobby.getX() - 1000)
                    lobby.setX(a.getLobbyLocation().getX() + 1000);
            }
        }
        Location arena = lobby.clone().add(new Vector(0, 0, 1000)); // Put the arena 1000 blocks south of the lobby
        
        Arena a = new Arena(mapName, lobby, arena);
        if (!arenas.containsKey(mapName))
            arenas.put(mapName, new HashSet<Arena>());
        arenas.get(mapName).add(a);
        
        maps.get(mapName).lobbySchematic.buildDelayed(lobby, plugin);
        maps.get(mapName).gameSchematic.buildDelayed(arena, plugin);
        
        return a;
    }
}
