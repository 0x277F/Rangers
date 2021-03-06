package net.coasterman10.rangers;

import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.arena.BossfightArena;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public class ArenaJoinSign extends ArenaSign {
    public ArenaJoinSign(Arena arena, Location location) {
        super(arena, location);
    }

    public void update() {
        if (hasArena()) {
            setLine(0, ChatColor.BOLD + arena.getName());
            setLine(1, arena.getType().getName());
            if (arena.getPlayerCount() < arena.getMaxPlayers()) {
                setLine(2, ChatColor.GREEN + "Right Click");
                setLine(3, ChatColor.GREEN + "To Join");
            } else {
                // Ugly hack
                if (arena instanceof BossfightArena) {
                    setLine(2, ChatColor.DARK_RED + "This Game");
                    setLine(3, ChatColor.DARK_RED + "Is Full");
                }
                setLine(2, ChatColor.RED + "This Game");
                setLine(3, ChatColor.RED + "Is Full");
            }
        }
    }
}
