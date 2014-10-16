package net.coasterman10.rangers.command;

import net.coasterman10.rangers.arena.ArenaManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RangersArenaAddCommand implements Subcommand {
    private final ArenaManager arenaManager;

    public RangersArenaAddCommand(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Adds a new arena";
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
            if (arenaManager.addArena(args[0])) {
                sender.sendMessage(ChatColor.GREEN + "Added arena \"" + args[0] + "\"");
            } else {
                sender.sendMessage(ChatColor.RED + "Arena with id \"" + arenaManager.getArena(args[0]).getId()
                        + "\" already exists");
            }
        }
    }
}
