package net.coasterman10.rangers.map;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.coasterman10.rangers.SpawnVector;
import net.coasterman10.rangers.config.ConfigAccessor;
import net.coasterman10.rangers.game.GameTeam;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class GameMapManager {
    private final ConfigAccessor config;
    private final File schematicFolder;
    private Map<String, GameMap> maps = new HashMap<>();

    public GameMapManager(ConfigAccessor config, File schematicFolder) {
        this.config = config;
        this.schematicFolder = schematicFolder;
    }

    public void loadMaps() {
        config.reload();
        for (String mapName : config.get().getKeys(false)) {
            maps.put(mapName, loadMap(mapName));
        }
    }

    public GameMap createMap(String name) {
        if (!maps.containsKey(name)) {
            GameMap map = new GameMap(name);
            maps.put(name, map);
            return map;
        } else {
            return maps.get(name);
        }
    }

    public GameMap getMap(String name) {
        return maps.get(name);
    }

    public Collection<GameMap> getMaps() {
        return maps.values();
    }

    public void saveMap(GameMap map) {
        ConfigurationSection section = config.get().createSection(map.name);

        section.set("schematic", map.getSchematic().getFile().getName());

        if (map.getLobbySpawn() != null) {
            ConfigurationSection lobby = section.createSection("lobby");
            setVector(map.getLobbySpawn(), lobby);
        }

        ConfigurationSection spawns = section.createSection("spawns");
        for (GameTeam team : GameTeam.values()) {
            if (map.getSpawn(team) != null) {
                ConfigurationSection spawn = spawns.createSection(team.name().toLowerCase());
                setVector(map.getSpawn(team), spawn);
            }
        }

        ConfigurationSection chests = section.createSection("chests");
        for (GameTeam team : GameTeam.values()) {
            if (map.getChest(team) != null) {
                ConfigurationSection chest = chests.createSection(team.name().toLowerCase());
                setVector(map.getChest(team), chest);
            }
        }

        config.save();
    }

    private GameMap loadMap(String name) {
        ConfigurationSection section = config.get().getConfigurationSection(name);
        if (section == null)
            return null;

        GameMap map = new GameMap(name);

        String schematicName = section.getString("schematic", name + ".schematic");
        File schematicFile = new File(schematicFolder, schematicName);
        Schematic s = new Schematic(schematicFile);
        try {
            s.load();
        } catch (IOException | InvalidSchematicException e) {
            e.printStackTrace();
        }
        map.setSchematic(s);

        ConfigurationSection lobby = section.getConfigurationSection("lobby");
        if (lobby != null) {
            map.setLobbySpawn(getVector(lobby));
        }

        ConfigurationSection spawns = section.getConfigurationSection("spawns");
        if (spawns != null) {
            for (GameTeam team : GameTeam.values()) {
                ConfigurationSection vec = spawns.getConfigurationSection(team.name().toLowerCase());
                SpawnVector sv = getVector(vec);
                map.setSpawn(team, sv);
            }
            
            ConfigurationSection vec = spawns.getConfigurationSection("spectators");
            SpawnVector sv = getVector(vec);
            map.setSpectatorSpawn(sv);
        }

        ConfigurationSection chests = section.getConfigurationSection("chests");
        if (chests != null) {
            for (GameTeam team : GameTeam.values()) {
                ConfigurationSection vec = chests.getConfigurationSection(team.name().toLowerCase());
                BlockVector bv = getVector(vec).toBlockVector();
                map.setChest(team, bv);
            }
        }

        return map;
    }

    private SpawnVector getVector(ConfigurationSection section) {
        if (section != null) {
            if (section.contains("x") && section.contains("y") && section.contains("z")) {
                double x = section.getDouble("x");
                double y = section.getDouble("y");
                double z = section.getDouble("z");
                SpawnVector sv = new SpawnVector(x, y, z);
                if (section.contains("yaw")) {
                    sv.setYaw((float) section.getDouble("yaw"));
                }
                if (section.contains("pitch")) {
                    sv.setPitch((float) section.getDouble("pitch"));
                }
                return sv;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private void setVector(Vector v, ConfigurationSection section) {
        if (v != null && section != null) {
            if (v instanceof BlockVector) {
                section.set("x", v.getBlockX());
                section.set("y", v.getBlockY());
                section.set("z", v.getBlockZ());
            } else {
                section.set("x", v.getX());
                section.set("y", v.getY());
                section.set("z", v.getZ());
                if (v instanceof SpawnVector) {
                    section.set("yaw", ((SpawnVector) v).getYaw());
                    section.set("pitch", ((SpawnVector) v).getPitch());
                }
            }
        }
    }
}
