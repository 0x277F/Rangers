package net.coasterman10.rangers.command.stats;

import java.util.UUID;

import net.coasterman10.rangers.command.Subcommand;
import net.coasterman10.rangers.stats.StatManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class StatsSetCommand implements Subcommand {
    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getDescription() {
        return "Manually modifies the statistics of a player";
    }

    @Override
    public String getArguments() {
        return "<subject> <stat> <value>";
    }

    @Override
    public String getPermission() {
        return "rangers.stats.modify";
    }

    @Override
    public boolean canConsoleUse() {
        return true;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(args.length != 3)
            return false;
        @SuppressWarnings("deprecation")
        UUID uuid = Bukkit.getPlayer(args[0]).getUniqueId();
        Object value = args[2];
        String statName = args[1];
        StatManager.update(uuid, statName, value);
        sender.sendMessage(ChatColor.GREEN + "Statistic " + statName + " for uuid " + uuid.toString() + " was updated to value " + StatManager.getStatistic(uuid).get(statName));
        return true;
    }
}
