package net.coasterman10.rangers.command.arena;

import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.command.Subcommand;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ArenaRenameCommand implements Subcommand {
    private final ArenaManager arenaManager;

    public ArenaRenameCommand(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public String getName() {
        return "rename";
    }

    @Override
    public String getDescription() {
        return "Renames an arena";
    }

    @Override
    public String getArguments() {
        return ChatColor.GREEN + "<oldName> <newName>";
    }

    @Override
    public String getPermission() {
        return "rangers.arena.build";
    }

    @Override
    public boolean canConsoleUse() {
        return true;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        } else {
            Arena a = arenaManager.getArena(args[0]);
            if (a != null) {
                StringBuilder name = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    name.append(args[i]);
                }
                if (a.rename(name.toString())) {
                    sender.sendMessage(ChatColor.GREEN + "Renamed arena \"" + a.getName() + "\" to \"" + ChatColor.AQUA
                            + a.getName() + ChatColor.GREEN + "\"");
                } else {
                    sender.sendMessage(ChatColor.RED + "Could not rename arena \"" + a.getName() + "\"");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "No such arena \"" + ChatColor.GOLD + args[0] + ChatColor.RED + "\"");
            }
            return true;
        }
    }
}
