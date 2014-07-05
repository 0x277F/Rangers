package net.coasterman10.rangers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
    private GameMapManager gameMapManager;
    private Map<String, Collection<Arena>> arenas = new HashMap<>();

    public ArenaManager(Plugin plugin, World world, GameMapManager gameMapManager) {
        this.plugin = plugin;
        this.world = world;
        this.gameMapManager = gameMapManager;

        arenaListFile = new File(plugin.getDataFolder(), "built-arenas.yml");
    }

    // This config isn't meant to be touched by human hands. There are no failsafes for now.
    // If you mess with the arena list file and it breaks, it's your fault.
    @SuppressWarnings("unchecked")
    public void loadArenas() {
        FileConfiguration arenaListConfig = YamlConfiguration.loadConfiguration(arenaListFile);
        List<Map<?, ?>> arenaList = arenaListConfig.getMapList("arenas");
        for (Map<?, ?> map : arenaList) {
            String mapName = (String) map.get("map");
            Map<String, Object> lobby = (Map<String, Object>) map.get("lobby");
            Map<String, Object> arena = (Map<String, Object>) map.get("arena");
            Location lobbyLocation = Vector.deserialize(lobby).toLocation(world);
            Location arenaLocation = Vector.deserialize(arena).toLocation(world);

            if (!arenas.containsKey(mapName)) {
                arenas.put(mapName, new HashSet<Arena>());
            }
            arenas.get(mapName).add(new Arena(gameMapManager.getMap(mapName), lobbyLocation, arenaLocation));
        }
    }

    @SuppressWarnings("unchecked")
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
                if (a.getOrigin().getX() > lobby.getX() - 1000)
                    lobby.setX(a.getOrigin().getX() + 1000);
            }
        }
        Location arena = lobby.clone().add(new Vector(0, 0, 1000)); // Put the arena 1000 blocks south of the lobby

        Arena a = new Arena(gameMapManager.getMap(mapName), lobby, arena);
        if (!arenas.containsKey(mapName))
            arenas.put(mapName, new HashSet<Arena>());
        arenas.get(mapName).add(a);

        gameMapManager.getMap(mapName).lobbySchematic.buildDelayed(lobby, plugin);
        gameMapManager.getMap(mapName).gameSchematic.buildDelayed(arena, plugin);

        FileConfiguration arenaListConfig = YamlConfiguration.loadConfiguration(arenaListFile);
        List<Map<String, Object>> arenaList = (List<Map<String, Object>>) arenaListConfig.getList("arenas",
                new ArrayList<>());
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("map", mapName);
        map.put("lobby", serializeVector(lobby.toVector()));
        map.put("arena", serializeVector(arena.toVector()));
        arenaList.add(map);
        arenaListConfig.set("arenas", arenaList);
        try {
            arenaListConfig.save(arenaListFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return a;
    }

    private static Map<String, Object> serializeVector(Vector v) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("x", v.getX());
        map.put("y", v.getY());
        map.put("z", v.getZ());
        return map;
    }
}
