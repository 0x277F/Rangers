package net.coasterman10.rangers.map;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.coasterman10.rangers.config.ConfigAccessor;

import org.bukkit.configuration.ConfigurationSection;

public class GameMapManager {
    private final ConfigAccessor config;
    private final File schematicFolder;
    private Map<String, GameMap> maps = new HashMap<>();

    public GameMapManager(ConfigAccessor config, File schematicFolder) {
        this.config = config;
        this.schematicFolder = schematicFolder;
    }

    public void loadMaps() {
        for (String mapName : config.get().getConfigurationSection("maps").getKeys(false)) {
            ConfigurationSection section = config.get().getConfigurationSection("maps." + mapName);
            GameMap map = new GameMap(mapName);
            map.load(section, schematicFolder);
            if (map.isValid())
                maps.put(mapName, map);
        }
    }
    
    public GameMap createMap(String name) {
        if (!maps.containsKey(name)) {
            GameMap map = new GameMap(name);
            map.save(config.get().getConfigurationSection("maps." + map.name));
            return map;
        } else {
            return maps.get(name);
        }
    }

    public GameMap getMap(String name) {
        return maps.get(name);
    }

    public void saveMap(GameMap map) {
        map.save(config.get().getConfigurationSection("maps." + map.name));
        config.save();
    }

    public Collection<GameMap> getMaps() {
        return maps.values();
    }
}
