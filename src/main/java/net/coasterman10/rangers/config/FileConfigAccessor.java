package net.coasterman10.rangers.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import net.coasterman10.rangers.Rangers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileConfigAccessor implements ConfigAccessor {
    private final File file;
    private FileConfiguration config;
    private InputStream defaults;

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
        
        for (Entry<String, Object> value : config.getDefaults().getValues(true).entrySet())
            if (!config.isSet(value.getKey()))
                config.set(value.getKey(), value.getValue());
        save();
    }

    @Override
    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            Rangers.instance().getLogger().warning("Could not save configuration to " + file);
        }
    }

    @Override
    public FileConfiguration get() {
        return config;
    }
}
