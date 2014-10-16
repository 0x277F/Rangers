package net.coasterman10.rangers.command;

import net.coasterman10.rangers.arena.ArenaManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RangersArenaRemoveCommand implements Subcommand {
    private final ArenaManager arenaManager;

    public RangersArenaRemoveCommand(ArenaManager arenaManager) {
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
    public void execute(CommandSender sender, String label, String[] args, Object[] data) {
        if (args.length > 0) {
            if (arenaManager.removeArena(args[0])) {
                sender.sendMessage(ChatColor.GREEN + "Successfully deleted arena \"" + args[0] + "\"");
            } else {
                sender.sendMessage(ChatColor.RED + "No such arena \"" + args[0] + "\"");
            }
        }
    }
}
