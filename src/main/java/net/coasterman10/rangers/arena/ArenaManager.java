package net.coasterman10.rangers.arena;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.coasterman10.rangers.config.FileConfigAccessor;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

public class ArenaManager {
    private final Plugin plugin;
    private File arenaFolder;
    private Map<String, Arena> arenas = new HashMap<>();

    public ArenaManager(Plugin plugin, File arenaFolder) {
        this.plugin = plugin;
        this.arenaFolder = arenaFolder;
    }
    
    public void loadArenas() {
        for (File file : arenaFolder.listFiles()) {
            FileConfigAccessor config = new FileConfigAccessor(file);
            ConfigurationSection conf = config.get();
            String name = conf.getString("name", null);
            ArenaType type = ArenaType.valueOf(conf.getString("type", null).toUpperCase());
            Arena arena = type.newInstance(name, config, plugin);
            arenas.put(name, arena);
            Bukkit.getPluginManager().registerEvents(arena, plugin);
        }
    }
    
    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public boolean addArena(String name, ArenaType type) {
        if (!arenas.containsKey(name)) {
            File arenaFile = new File(arenaFolder, name + ".yml");
            FileConfigAccessor config = new FileConfigAccessor(arenaFile);
            Arena arena = type.newInstance(name, config, plugin);
            arena.load();
            arenas.put(name, arena);
            Bukkit.getPluginManager().registerEvents(arena, plugin);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeArena(String name) {
        if (arenas.containsKey(name)) {
            Arena arena = arenas.remove(name);
            arena.unload();
            new File(arenaFolder, name + ".yml").delete();
            return true;
        } else {
            return false;
        }
    }

    public Collection<Arena> getArenas() {
        return Collections.unmodifiableCollection(arenas.values());
    }
}
