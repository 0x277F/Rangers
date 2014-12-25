package net.coasterman10.rangers;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public class ArenaJoinSign extends ArenaSign {
    public ArenaJoinSign(Location location) {
        super(location);
    }

    public void update() {
        if (hasArena()) {
            setLine(0, ChatColor.BOLD + arena.getName());
            setLine(1, "Classic");
            if (arena.getPlayerCount() < arena.getMaxPlayers()) {
                setLine(2, ChatColor.GREEN + "Right Click");
                setLine(3, ChatColor.GREEN + "To Join");
            } else {
                setLine(2, ChatColor.RED + "This Game");
                setLine(3, ChatColor.RED + "Is Full");
            }
        }
    }
}
