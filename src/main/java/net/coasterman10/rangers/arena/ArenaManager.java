package net.coasterman10.rangers.arena;

import java.util.HashMap;
import java.util.Map;

import net.coasterman10.rangers.config.ConfigAccessor;

import org.bukkit.configuration.ConfigurationSection;

public class ArenaManager {
    private final ConfigAccessor config;
    private Map<String, Arena> arenas = new HashMap<>();

    public ArenaManager(ConfigAccessor config) {
        this.config = config;
    }

    public void loadArenas() {
        ConfigurationSection arenaConfig = config.get();
        for (String name : arenaConfig.getKeys(false)) {
            ConfigurationSection section = arenaConfig.getConfigurationSection(name);
            Arena arena = new Arena(name);
            arena.load(section);
            arenas.put(arena.getId(), arena);
        }
    }
    
    public Arena getArena(String name) {
        return arenas.get(name);
    }
}
