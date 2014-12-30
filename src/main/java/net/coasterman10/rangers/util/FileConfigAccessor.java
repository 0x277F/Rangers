package net.coasterman10.rangers.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileConfigAccessor implements ConfigAccessor {
    private final File file;
    private FileConfiguration config;
    private InputStream defaults;

    public FileConfigAccessor(File file) {
        this.file = file;
        defaults = null;
    }

    public FileConfigAccessor(File file, InputStream defaults) {
        this.file = file;
        this.defaults = defaults;
    }

    @Override
    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);

        if (defaults != null) {
            @SuppressWarnings("deprecation")
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaults);
            config.setDefaults(defaultConfig);
        }
        
        save();
    }

    @Override
    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().warning("Could not save configuration to " + file);
        }
    }

    @Override
    public FileConfiguration get() {
        if (config == null)
            reload();
        return config;
    }

    public File getFile() {
        return file;
    }
}
