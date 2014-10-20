package net.coasterman10.rangers.command.arena;

import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.command.Subcommand;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ArenaRemoveCommand implements Subcommand {
    private final ArenaManager arenaManager;

    public ArenaRemoveCommand(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Removes an arena";
    }

    @Override
    public String getArguments() {
        return ChatColor.GREEN + "<id>";
    }

    @Override
    public boolean canConsoleUse() {
        return true;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        } else {
            if (arenaManager.removeArena(args[0])) {
                sender.sendMessage(ChatColor.GREEN + "Successfully deleted arena \"" + args[0] + "\"");
            } else {
                sender.sendMessage(ChatColor.RED + "No such arena \"" + args[0] + "\"");
            }
            return true;
        }
    }
}
