package net.coasterman10.rangers.command.arena;

import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.arena.ArenaType;
import net.coasterman10.rangers.command.Subcommand;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ArenaAddCommand implements Subcommand {
    private final ArenaManager arenaManager;

    public ArenaAddCommand(ArenaManager arenaManager) {
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
        return ChatColor.GREEN + "<id> <classic|war|bossfight>";
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
            try {
                ArenaType type = ArenaType.valueOf(args[1].toUpperCase());
                if (arenaManager.addArena(args[0], type)) {
                    sender.sendMessage(ChatColor.GREEN + "Added arena \"" + args[0] + "\"");
                } else {
                    sender.sendMessage(ChatColor.RED + "Arena with id \"" + arenaManager.getArena(args[0]).getName()
                            + "\" already exists");
                }
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }
}
