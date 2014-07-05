package net.coasterman10.rangers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.coasterman10.rangers.config.ConfigAccessor;

import org.bukkit.configuration.ConfigurationSection;
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
        // Schematic for the game lobbies
        String lobbySchematicFilename = "schematics" + File.separator + config.get().getString("game-lobby.schematic");
        File lobbySchematicFile = new File(schematicFolder, lobbySchematicFilename);
        Schematic lobbySchematic;
        try {
            lobbySchematic = new Schematic(lobbySchematicFile);
        } catch (IOException | InvalidSchematicException e) {
            Rangers.logger().warning("Could not load game lobby schematic: " + e.getMessage());
            Rangers.logger().warning("Loading default empty schematic for game lobby.");
            lobbySchematic = new Schematic();
        }

        // Vector containing the offset of the lobby spawn from its origin
        Vector gameLobbySpawn = getVector(config.get().getConfigurationSection("game-lobby.spawn"));

        for (String mapName : config.get().getConfigurationSection("maps").getKeys(false)) {
            ConfigurationSection section = config.get().getConfigurationSection("maps." + mapName);
            GameMap map = new GameMap(mapName);
            map.lobbySchematic = lobbySchematic;
            try {
                map.gameSchematic = new Schematic(new File(schematicFolder, "schematics" + File.separator
                        + config.get().getString("maps." + mapName + ".schematic")));
            } catch (IOException | InvalidSchematicException e) {
                Rangers.logger().warning(
                        "Could not load game map schematic for map " + mapName + ": " + e.getMessage());
                Rangers.logger().warning("Loading default empty schematic for map " + mapName);
                map.gameSchematic = new Schematic();
            }
            map.rangerSpawn = getVector(section.getConfigurationSection("spawns.rangers"));
            map.banditSpawn = getVector(section.getConfigurationSection("spawns.bandits"));
            map.rangerChest = getVector(section.getConfigurationSection("chests.rangers")).toBlockVector();
            map.banditChest = getVector(section.getConfigurationSection("chests.bandits")).toBlockVector();
            map.lobbySpawn = gameLobbySpawn;
            maps.put(mapName, map);
        }
    }

    public GameMap getMap(String name) {
        return maps.get(name);
    }

    private static Vector getVector(ConfigurationSection config) {
        if (config.isSet("x") && config.isSet("y") && config.isSet("z"))
            return new Vector(config.getDouble("x"), config.getDouble("y"), config.getDouble("z"));
        else
            return new Vector();
    }
}
