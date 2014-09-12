package net.coasterman10.rangers.map;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import net.coasterman10.rangers.InvalidSchematicException;
import net.coasterman10.rangers.SpawnVector;
import net.coasterman10.rangers.game.GameTeam;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class GameMap {
    public final String name;
    private Schematic schematic;
    private SpawnVector lobbySpawn;
    private Map<GameTeam, SpawnVector> spawns = new EnumMap<>(GameTeam.class);
    private Map<GameTeam, BlockVector> chests = new EnumMap<>(GameTeam.class);

    public GameMap(String name) {
        this.name = name;
    }

    public void load(ConfigurationSection config, File schematicFolder) {
        try {
            schematic = new Schematic(new File(schematicFolder, config.getString("schematic")));
        } catch (IOException | InvalidSchematicException e) {
            e.printStackTrace();
            schematic = null;
        }

        lobbySpawn = getVector(config.getConfigurationSection("lobby"));
        for (GameTeam team : GameTeam.values()) {
            String name = team.name().toLowerCase();
            spawns.put(team, getVector(config.getConfigurationSection("spawns." + name)));
            if (team != GameTeam.SPECTATORS) {
                chests.put(team, getVector(config.getConfigurationSection("chests." + name)).toBlockVector());
            }
        }
    }

    public void save(ConfigurationSection config) {
        if (schematic != null)
            config.set("schematic", schematic.getFilename());
        setVector(config.createSection("lobby"), lobbySpawn);
        config.createSection("spawns");
        config.createSection("chests");
        for (GameTeam team : GameTeam.values()) {
            String name = team.name().toLowerCase();
            setVector(config.createSection("spawns." + name), spawns.get(team));
            if (team != GameTeam.SPECTATORS) {
                setVector(config.createSection("chests." + name), chests.get(team));
            }
        }
    }

    public Schematic getSchematic() {
        return schematic;
    }

    public SpawnVector getLobbySpawn() {
        return lobbySpawn;
    }

    public SpawnVector getSpawn(GameTeam team) {
        return spawns.get(team);
    }

    public BlockVector getChest(GameTeam team) {
        return chests.get(team);
    }

    public void setSchematic(Schematic schematic) {
        this.schematic = schematic;
    }

    public void setLobbySpawn(SpawnVector lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    public void setSpawn(GameTeam team, SpawnVector spawn) {
        spawns.put(team, spawn);
    }

    public void setChest(GameTeam team, BlockVector chest) {
        chests.put(team, chest);
    }

    public boolean isValid() {
        if (schematic == null || lobbySpawn == null)
            return false;
        for (GameTeam team : GameTeam.values()) {
            if (!spawns.containsKey(team))
                return false;
            if (!chests.containsKey(team) && team != GameTeam.SPECTATORS)
                return false;
        }
        return true;
    }

    private static SpawnVector getVector(ConfigurationSection config) {
        if (config != null) {
            SpawnVector vec = new SpawnVector();
            if (config.isSet("x") && config.isSet("y") && config.isSet("z"))
                vec.setX(config.getDouble("x")).setY(config.getDouble("y")).setZ(config.getDouble("z"));
            else
                return null;
            if (config.isSet("yaw"))
                vec.setYaw((float) config.getDouble("yaw"));
            if (config.isSet("pitch"))
                vec.setPitch((float) config.getDouble("pitch"));
            return vec;
        } else {
            return null;
        }
    }

    private static void setVector(ConfigurationSection config, Vector vector) {
        if (config != null && vector != null) {
            if (vector instanceof BlockVector) {
                config.set("x", vector.getBlockX());
                config.set("y", vector.getBlockY());
                config.set("z", vector.getBlockZ());
            } else {
                config.set("x", vector.getX());
                config.set("y", vector.getY());
                config.set("z", vector.getZ());
            }
            if (vector instanceof SpawnVector) {
                config.set("yaw", ((SpawnVector) vector).getYaw());
                config.set("pitch", ((SpawnVector) vector).getPitch());
            }
        }
    }
}
