package net.coasterman10.rangers.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

public class PluginConfigAccessor implements ConfigAccessor {
    private final Plugin plugin;
    
    public PluginConfigAccessor(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void reload() {
        plugin.reloadConfig();
    }

    @Override
    public void save() {
        plugin.saveConfig();
    }

    @Override
    public ConfigurationSection get() {
        return plugin.getConfig();
    } 
}
