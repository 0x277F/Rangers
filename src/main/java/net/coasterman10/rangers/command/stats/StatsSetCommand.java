package net.coasterman10.rangers.command.stats;

import net.coasterman10.rangers.command.Subcommand;
import net.coasterman10.rangers.stats.StatManager;
import net.coasterman10.rangers.stats.Statistic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Field;
import java.util.UUID;

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
        UUID uuid = Bukkit.getPlayer(args[0]).getUniqueId();
        Statistic s = StatManager.getStatistic(uuid);
        Object value = args[2];
        String statName = args[1];

        boolean foundStat = false;

        for(Field f : s.getClass().getDeclaredFields()){
            if(f.getName().equals(statName)){
                foundStat = true;
                f.setAccessible(true);
                try {
                    f.set(null, value);
                    sender.sendMessage(ChatColor.GREEN + "Statistic " + statName + " for uuid " + uuid.toString() + " was updated to value " + value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        if(!foundStat)
            sender.sendMessage(ChatColor.RED + "Cannot find specified statistic!");

        return true;
    }
}
