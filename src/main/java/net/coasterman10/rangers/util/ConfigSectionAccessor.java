package net.coasterman10.rangers.util;

import org.bukkit.configuration.ConfigurationSection;

public class ConfigSectionAccessor implements ConfigAccessor {
    private final ConfigAccessor parent;
    private final String path;

    public ConfigSectionAccessor(ConfigAccessor parent, String path) {
        this.parent = parent;
        this.path = path;
    }

    @Override
    public void reload() {
        parent.reload();
    }

    @Override
    public void save() {
        parent.save();
    }

    @Override
    public ConfigurationSection get() {
        ConfigurationSection conf = parent.get().getConfigurationSection(path);
        if (conf == null) {
            return parent.get().createSection(path);
        } else {
            return conf;
        }
    }
}
