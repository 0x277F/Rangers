package net.coasterman10.rangers;

import net.coasterman10.rangers.game.Game;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public class ArenaJoinSign extends ArenaSign {
    public ArenaJoinSign(Location location) {
        super(location);
    }

    public void update() {
        if (hasGame()) {
            Game game = arena.getGame();
            setLine(0, ChatColor.BOLD + game.getArena().getName());
            setLine(1, "Classic");
            if (game.getPlayerCount() < game.getSettings().maxPlayers) {
                setLine(2, ChatColor.GREEN + "Right Click");
                setLine(3, ChatColor.GREEN + "To Join");
            } else {
                setLine(2, ChatColor.RED + "This Game");
                setLine(3, ChatColor.RED + "Is Full");
            }
        }
    }
}
