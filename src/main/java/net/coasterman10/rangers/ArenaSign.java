package net.coasterman10.rangers;

import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.game.Game;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public abstract class ArenaSign {
    protected final Location location;
    protected Arena arena;

    public ArenaSign(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setArena(Arena arena) {
        this.arena = arena;
    }

    public Arena getArena() {
        return arena;
    }

    public Game getGame() {
        return arena.getGame();
    }

    public abstract void update();

    protected void setLine(int index, String line) {
        Block b = location.getBlock();
        if (!b.getChunk().isLoaded())
            b.getChunk().load();
        BlockState state = b.getState();
        if (state instanceof Sign) {
            ((Sign) state).setLine(index, line);
            state.update();
        }
    }
    
    public boolean hasGame() {
        return arena != null && arena.hasGame();
    }
}
