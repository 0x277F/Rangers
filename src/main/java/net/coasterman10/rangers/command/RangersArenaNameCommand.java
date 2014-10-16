package net.coasterman10.rangers.command;

import net.coasterman10.rangers.arena.Arena;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RangersArenaNameCommand implements Subcommand {
    @Override
    public String getName() {
        return "name";
    }

    @Override
    public String getDescription() {
        return "Sets the name for an arena";
    }

    @Override
    public String getArguments() {
        return ChatColor.BLUE + "[value]";
    }

    @Override
    public boolean canConsoleUse() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args, Object[] data) {
        Arena a = (Arena) data[0];
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "Arena \"" + ChatColor.YELLOW + a.getId() + ChatColor.GOLD
                    + "\" name = \"" + ChatColor.YELLOW + a.getName() + ChatColor.GOLD + "\"");
        } else {
            a.setName(args[0]);
            sender.sendMessage(ChatColor.GREEN + "Upated arena \"" + ChatColor.YELLOW + a.getId() + ChatColor.GREEN
                    + "\" name = \"" + ChatColor.YELLOW + a.getName() + ChatColor.GREEN + "\"");
            
            // TODO Save arena
        }
    }
}
