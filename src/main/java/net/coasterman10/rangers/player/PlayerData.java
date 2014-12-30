package net.coasterman10.rangers.player;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class PlayerData {
    private static Logger logger;
    private static File dataFolder;
    private static Configuration defaultConfig;

    public static void initialize(Plugin plugin) {
        logger = plugin.getLogger();
        dataFolder = new File(plugin.getDataFolder(), "players");
        dataFolder.mkdirs();
        defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("player.yml")));
    }

    private static File getDataFile(RangersPlayer player) {
        return new File(dataFolder, player.getUniqueId() + ".yml");
    }

    private final File dataFile;
    private FileConfiguration config;

    public PlayerData(RangersPlayer player) {
        dataFile = getDataFile(player);
        config = new YamlConfiguration();
        config.setDefaults(defaultConfig);
        config.options().copyDefaults(true);
        try {
            if (!dataFile.exists()) {
                try {
                    dataFile.createNewFile();
                    config.save(dataFile);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Could not create player data file " + dataFile, e);
                }
            } else {
                config.load(dataFile);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not read player data file " + dataFile, e);
        } catch (InvalidConfigurationException e) {
            logger.warning("Player data file " + dataFile + " contains an invalid or corrupt YAML configuration");
        }
    }
    
    public void save() {
        try {
            config.save(dataFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save player data file " + dataFile, e);
        }
    }
    
    public String getUpgradeSelection(String key) {
        return config.getString("upgrades." + key);
    }
    
    public boolean isUpgradeSelected(String key, String value) {
        return getUpgradeSelection(key).equals(value);
    }
    
    public void setUpgradeSelection(String key, String value) {
        config.set("upgrades." + key, value);
    }
}
