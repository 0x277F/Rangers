package net.coasterman10.rangers.arena;

import net.coasterman10.rangers.game.RangersTeam;
import net.coasterman10.rangers.util.FileConfigAccessor;

import org.bukkit.plugin.Plugin;

public class BossfightArena extends Arena {
    public BossfightArena(String name, FileConfigAccessor config, Plugin plugin) {
        super(name, config, plugin);
    }
    
    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }
        for (RangersTeam team : RangersTeam.values()) {
            if (spawns.get(team) == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ArenaType getType() {
        return ArenaType.BOSSFIGHT;
    }
}
