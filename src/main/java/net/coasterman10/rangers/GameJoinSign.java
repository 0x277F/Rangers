package net.coasterman10.rangers;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public class GameJoinSign extends GameSign {
    public GameJoinSign(Location location) {
        super(location);
    }

    public void update() {
        if (game != null) {
            setLine(0, ChatColor.BOLD + game.getMapName());
            setLine(1, "Classic");
            if (game.getPlayerCount() < game.getSettings().maxPlayers) {
                setLine(2, ChatColor.GREEN + "Right Click");
                setLine(3, ChatColor.GREEN + "To Join");
            } else {
                setLine(2, ChatColor.RED + "This Game");
                setLine(3, ChatColor.RED + "Is Full");
            }
        } else {
            setLine(0, "");
            setLine(1, "N / A");
            setLine(2, "");
            setLine(3, "");
        }
    }
}
