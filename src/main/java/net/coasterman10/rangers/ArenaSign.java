package net.coasterman10.rangers;

import net.coasterman10.rangers.arena.Arena;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public abstract class ArenaSign {
    protected final Arena arena;
    protected final Location location;

    public ArenaSign(Arena arena, Location location) {
        this.arena = arena;
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public Arena getArena() {
        return arena;
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
    
    public boolean hasArena() {
        return arena != null;
    }
}
