package net.coasterman10.rangers.arena;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.coasterman10.rangers.config.ConfigAccessor;
import net.coasterman10.rangers.config.ConfigSectionAccessor;

import org.bukkit.configuration.ConfigurationSection;

public class ArenaManager {
    private final ConfigAccessor config;
    private Map<String, Arena> arenas = new HashMap<>();

    public ArenaManager(ConfigAccessor config) {
        this.config = config;
    }

    public void loadArenas() {
        ConfigurationSection arenaConfig = config.get();
        if (arenaConfig == null)
            return;
        for (String id : arenaConfig.getKeys(false)) {
            Arena arena = new Arena(id, new ConfigSectionAccessor(config, id));
            arena.load();
            arenas.put(arena.getId().toLowerCase(), arena);
        }
    }

    public boolean addArena(String id) {
        if (getArena(id) == null) {
            arenas.put(id, new Arena(id, new ConfigSectionAccessor(config, id)));
            config.get().createSection(id);
            config.save();
            return true;
        } else {
            return false;
        }
    }

    public boolean removeArena(String id) {
        if (getArena(id) != null) {
            arenas.remove(id);
            config.get().set(id, null);
            config.save();
            return true;
        } else {
            return false;
        }
    }

    public Arena getArena(String id) {
        return arenas.get(id.toLowerCase());
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }
}
