package net.coasterman10.rangers.arena;

import net.coasterman10.rangers.util.FileConfigAccessor;

import org.bukkit.plugin.Plugin;

public enum ArenaType {
    CLASSIC {
        @Override
        public Arena newInstance(String name, FileConfigAccessor config, Plugin plugin) {
            return new ClassicArena(name, config, plugin);
        }
    },
    WAR {
        @Override
        public Arena newInstance(String name, FileConfigAccessor config, Plugin plugin) {
            return new WarArena(name, config, plugin);
        }
    },
    BOSSFIGHT {
        @Override
        public Arena newInstance(String name, FileConfigAccessor config, Plugin plugin) {
            return new BossfightArena(name, config, plugin);
        }
    };

    public abstract Arena newInstance(String name, FileConfigAccessor config, Plugin plugin);

    public String getName() {
        return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
    }
}
