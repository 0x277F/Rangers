package net.coasterman10.rangers.config;

import org.bukkit.configuration.file.FileConfiguration;

public interface ConfigAccessor {
    void reload();

    void save();

    FileConfiguration get();
}
