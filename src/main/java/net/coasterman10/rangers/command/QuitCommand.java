package net.coasterman10.rangers.command;

import net.coasterman10.rangers.Rangers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuitCommand implements CommandExecutor {
    private final Rangers plugin;

    public QuitCommand(Rangers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1)
            return false;
        if (sender instanceof Player && args.length == 0) {
            plugin.sendToLobby((Player) sender);
        }
        if(args.length == 1 && sender.hasPermission("rangers.quit.others")){
            plugin.sendToLobby(Bukkit.getPlayer(args[0]));
        }
        return true;
    }
}
