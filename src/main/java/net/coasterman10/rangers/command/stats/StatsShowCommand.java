package net.coasterman10.rangers.command.stats;

import net.coasterman10.rangers.command.Subcommand;
import net.coasterman10.rangers.stats.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsShowCommand implements Subcommand {
    @Override
    public String getName() {
        return "show";
    }

    @Override
    public String getDescription() {
        return "Shows the stats of you or another player that is online";
    }

    @Override
    public String getArguments() {
        return "[Player]";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean canConsoleUse() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(args.length > 1)
            return false;
        if(sender instanceof Player){
            Player target = args.length == 0 ? (Player) sender : Bukkit.getPlayer(args[0]);
            StatManager.showStatGui(target, (Player) sender);
        }
        return true;
    }
}
