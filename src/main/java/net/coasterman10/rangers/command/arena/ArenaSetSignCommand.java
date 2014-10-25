package net.coasterman10.rangers.command.arena;

import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.command.Subcommand;
import net.coasterman10.rangers.listeners.SignManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaSetSignCommand implements Subcommand {
    private final ArenaManager arenaManager;
    private final SignManager signManager;

    public ArenaSetSignCommand(ArenaManager arenaManager, SignManager signManager) {
        this.arenaManager = arenaManager;
        this.signManager = signManager;
    }

    @Override
    public String getName() {
        return "setsign";
    }

    @Override
    public String getDescription() {
        return "Sets join and status signs for arenas";
    }

    @Override
    public String getArguments() {
        return ChatColor.GREEN + "<id> <join|status>";
    }

    @Override
    public boolean canConsoleUse() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("rangers.arena.build")){
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
            return true;
        }
        if (args.length < 2) {
            return false;
        } else {
            Arena a = arenaManager.getArena(args[0]);
            if (a != null) {
                int type = -1; // 0 is Join, 1 is Status
                switch (args[1].toLowerCase()) {
                case "join":
                    type = 0;
                    break;
                case "status":
                    type = 1;
                    break;
                default:
                    return false;
                }

                @SuppressWarnings("deprecation")
                Location sign = ((Player) sender).getTargetBlock(null, 50).getLocation();
                if (sign != null) {
                    switch (type) {
                    case 0:
                        signManager.addJoinSign(a, sign);
                        break;
                    case 1:
                        signManager.addStatusSign(a, sign);
                        break;
                    }
                    sender.sendMessage(ChatColor.GREEN + "Set " + (type == 0 ? "join" : "status")
                            + " sign for arena \"" + ChatColor.YELLOW + a.getId() + ChatColor.GREEN + "\" to "
                            + ChatColor.AQUA + "(" + sign.getX() + "," + sign.getY() + "," + sign.getZ() + ")");
                    a.save();
                } else {
                    sender.sendMessage(ChatColor.RED + "You must be targeting a block to set the sign location.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "No such arena \"" + ChatColor.GOLD + args[0] + ChatColor.RED + "\"");
            }
            return true;
        }
    }
}
