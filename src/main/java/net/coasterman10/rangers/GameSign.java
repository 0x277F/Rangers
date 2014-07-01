package net.coasterman10.rangers;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class GameSign {
    private Sign state;
    private Game game;

    public GameSign(Block b) {
        Validate.notNull(b);
        if (!(b.getState() instanceof Sign))
            throw new IllegalArgumentException("Block " + b + " is not a sign");
        state = (Sign) b.getState();
    }

    public Location getLocation() {
        return state.getLocation();
    }

    public void setPlayers(int players) {
        String s = String.format("%s%d / %d", ChatColor.BOLD.toString(), players, Game.MAX_PLAYERS);
        if (players == Game.MAX_PLAYERS) {
            s = ChatColor.RED + s;
            state.setLine(3, ChatColor.RED + "Full");
        } else {
            state.setLine(3, ChatColor.GREEN + "Click to join");
        }
        state.setLine(0, s);
    }

    public void setMapName(String mapName) {
        Validate.notNull(mapName);
        state.setLine(1, mapName);
    }

    public void setStatusMessage(String statusMessage) {
        state.setLine(2, statusMessage);
    }

    public void update() {
        state.update();
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }
}
