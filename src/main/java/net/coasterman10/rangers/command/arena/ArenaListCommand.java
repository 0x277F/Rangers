package net.coasterman10.rangers.command.arena;

import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.command.Subcommand;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ArenaListCommand implements Subcommand {
    private final ArenaManager arenaManager;

    public ArenaListCommand(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Lists all arenas";
    }

    @Override
    public String getArguments() {
        return "";
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
        sender.sendMessage(ChatColor.GOLD + "Arenas");
        for (Arena a : arenaManager.getArenas()) {
            StringBuilder sb = new StringBuilder(32);
            sb.append(ChatColor.GOLD).append("- ").append(ChatColor.AQUA).append(a.getId());
            sb.append(ChatColor.GOLD).append(" - ");
            sb.append(ChatColor.GREEN).append("\"").append(a.getName()).append("\"");
            sb.append(ChatColor.GOLD).append(" - ");
            sb.append(a.isActive() ? ChatColor.GREEN : (a.isValid() ? ChatColor.YELLOW : ChatColor.RED));
            sb.append(a.isActive() ? "Active" : (a.isValid() ? "Unused" : "Invalid"));
            sender.sendMessage(sb.toString());
        }
        return true;
    }
}
