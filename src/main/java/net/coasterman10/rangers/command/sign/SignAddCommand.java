package net.coasterman10.rangers.command.sign;

import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.command.Subcommand;
import net.coasterman10.rangers.listeners.SignManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SignAddCommand implements Subcommand {
    private final SignManager signManager;
    private final ArenaManager arenaManager;

    public SignAddCommand(SignManager signManager, ArenaManager arenaManager) {
        this.signManager = signManager;
        this.arenaManager = arenaManager;
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Adds a new sign for an arena";
    }

    @Override
    public String getArguments() {
        return ChatColor.GREEN + "<id> <join|status";
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
        if (args.length != 2) {
            return false;
        } else {
            Arena a = arenaManager.getArena(args[0]);
            if (a == null) {
                sender.sendMessage(ChatColor.RED + "No such arena \"" + ChatColor.GOLD + args[0] + ChatColor.RED + "\"");
                return true;
            } else {
                @SuppressWarnings("deprecation")
                Location sign = ((Player) sender).getTargetBlock(null, 50).getLocation();
                if (sign == null
                        || !(sign.getBlock().getType() == Material.SIGN || sign.getBlock().getType() == Material.SIGN_POST)) {
                    sender.sendMessage(ChatColor.RED + "You must be targeting a sign.");
                    return true;
                }
                if (args[1].equalsIgnoreCase("join")) {
                    signManager.addJoinSign(a, sign);
                    sender.sendMessage(ChatColor.GREEN + "Added join sign for arena \"" + ChatColor.YELLOW
                            + a.getName() + ChatColor.GREEN + "\" at " + ChatColor.AQUA + "(" + sign.getX() + ","
                            + sign.getY() + "," + sign.getZ() + ")");
                } else if (args[1].equalsIgnoreCase("status")) {
                    signManager.addStatusSign(a, sign);
                    sender.sendMessage(ChatColor.GREEN + "Added status sign for arena \"" + ChatColor.YELLOW
                            + a.getName() + ChatColor.GREEN + "\" at " + ChatColor.AQUA + "(" + sign.getX() + ","
                            + sign.getY() + "," + sign.getZ() + ")");
                } else {
                    return false;
                }
                return true;
            }
        }
    }
}
