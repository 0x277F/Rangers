package net.coasterman10.rangers;

import net.coasterman10.rangers.game.Game;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public abstract class GameSign {
    protected final Location location;
    protected Game game;

    public GameSign(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
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
}
