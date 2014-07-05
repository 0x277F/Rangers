package net.coasterman10.rangers;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public class GameSign {
    private Block block;
    private Game game;
    private String mapName; // TODO Remove this cheap hack

    public GameSign(Block block, String mapName) {
        Validate.notNull(block);
        this.block = block;

        this.mapName = mapName;
    }

    public Location getLocation() {
        return block.getLocation();
    }

    public void setPlayers(int players) {
        String s = String.format("%s%d / %d", ChatColor.BOLD.toString(), players, Game.MAX_PLAYERS);
        if (players == Game.MAX_PLAYERS) {
            s = ChatColor.RED + s;
            setLine(3, ChatColor.RED + "Full");
        } else {
            setLine(3, ChatColor.GREEN + "Click to join");
        }
        setLine(0, s);
    }

    public void setMapName(String mapName) {
        setLine(1, mapName);
    }

    public void setStatusMessage(String statusMessage) {
        setLine(2, statusMessage);
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }
    
    private void setLine(int index, String line) {
        BlockState state = block.getState();
        if (state instanceof Sign) {
            ((Sign) state).setLine(index, line);
            state.update();
        }
    }

    public String getMapName() {
        return mapName;
    }
}
