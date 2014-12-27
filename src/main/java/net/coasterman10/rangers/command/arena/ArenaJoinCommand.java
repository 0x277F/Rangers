package net.coasterman10.rangers.command.arena;

import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.command.Subcommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaJoinCommand implements Subcommand {
    private final ArenaManager arenaManager;

    public ArenaJoinCommand(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Joins a game";
    }

    @Override
    public String getArguments() {
        return "<id> [player]";
    }
    
    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean canConsoleUse() {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length > 2)
            return false;
        
        Player p;
        if (args.length == 1 && sender.hasPermission("rangers.join.self")) {
            if (sender instanceof Player) {
                p = (Player) sender;
            } else {
                sender.sendMessage("You must be a player to join a game!");
                return true;
            }
        } else if (sender.hasPermission("rangers.join.others")) {
            p = Bukkit.getPlayer(args[1]);
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission to join other players to a game.");
            return true;
        }
        
        Arena arena = arenaManager.getArena(args[0]);
        if (arena != null) {
            PlayerManager.getPlayer(p).joinArena(arena);
        } else {
            sender.sendMessage(ChatColor.RED + "No arena with id \"" + args[0] + "\"");
        }
        return true;
    }
}
