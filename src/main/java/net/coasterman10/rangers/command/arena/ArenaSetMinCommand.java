package net.coasterman10.rangers.command.arena;

import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.command.Subcommand;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaSetMinCommand implements Subcommand {
    private final ArenaManager arenaManager;

    public ArenaSetMinCommand(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public String getName() {
        return "setmin";
    }

    @Override
    public String getDescription() {
        return "Sets the minimum bound for an arena";
    }

    @Override
    public String getArguments() {
        return ChatColor.GREEN + "<id>";
    }

    @Override
    public String getPermission() {
        return "rangers.arena.build";
    }

    @Override
    public boolean canConsoleUse() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        } else {
            Arena a = arenaManager.getArena(args[0]);
            if (a != null) {
                Player player = (Player) sender;
                @SuppressWarnings("deprecation")
                Block targetBlock = player.getTargetBlock(null, 50);
                Location target;
                if (targetBlock != null) {
                    target = targetBlock.getLocation();
                } else {
                    player.sendMessage(ChatColor.GOLD
                            + "You are not targeting a block, so your current location has been used instead.");
                    target = player.getLocation();
                }
                a.setMin(target);
                sender.sendMessage(ChatColor.GREEN + "Set minimum bound of arena \"" + a.getName() + "\" to "
                        + ChatColor.AQUA + "(" + target.getBlockX() + "," + target.getBlockY() + ","
                        + target.getBlockZ() + ")");
                a.save();
            } else {
                sender.sendMessage(ChatColor.RED + "No such arena \"" + ChatColor.GOLD + args[0] + ChatColor.RED + "\"");
            }
            return true;
        }
    }
}
