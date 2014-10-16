package net.coasterman10.rangers.command;

import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.arena.ArenaManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RangersArenaListCommand implements Subcommand {
    private final ArenaManager arenaManager;

    public RangersArenaListCommand(ArenaManager arenaManager) {
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
    public boolean canConsoleUse() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args, Object[] data) {
        sender.sendMessage(ChatColor.GOLD + "Arenas");
        for (Arena a : arenaManager.getArenas()) {
            StringBuilder sb = new StringBuilder(32);
            sb.append(ChatColor.GOLD).append("- ").append(ChatColor.AQUA).append(a.getId());
            sb.append(ChatColor.GOLD).append(" - ");
            sb.append(ChatColor.GREEN).append("\"").append(a.getName()).append("\"");
            sb.append(ChatColor.GOLD).append(" - ");
            sb.append(a.isUsed() ? ChatColor.GREEN : (a.isValid() ? ChatColor.YELLOW : ChatColor.RED));
            sb.append(a.isUsed() ? "Active" : (a.isValid() ? "Unused" : "Invalid"));
            sender.sendMessage(sb.toString());
        }
    }
}
