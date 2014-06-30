package net.coasterman10.rangers.config;

import org.bukkit.configuration.ConfigurationSection;

public interface ConfigAccessor {
    void reload();
    
    void save();
    
    ConfigurationSection get();
}
