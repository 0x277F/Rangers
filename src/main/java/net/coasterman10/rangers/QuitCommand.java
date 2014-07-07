package net.coasterman10.rangers;

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
        if (args.length > 0)
            return false;
        if (sender instanceof Player) {
            Player p = (Player) sender;
            GamePlayer data = plugin.getPlayerData(p);
            if (data.getGame() != null) {
                data.getGame().removePlayer(p);
                p.setHealth(20D);
                p.setFoodLevel(20);
                p.setSaturation(20F);
                p.getInventory().clear();
                p.getInventory().setArmorContents(null);
                p.teleport(plugin.getLobbySpawn());
            }
        } else {
            sender.sendMessage("Only players can use this command");
        }
        return true;
    }
}
