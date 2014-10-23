package net.coasterman10.rangers.command.arena;

import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.command.Subcommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ArenaJoinCommand implements Subcommand {

    private Rangers plugin;
    public ArenaJoinCommand(Rangers r){
        this.plugin = r;
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Joins a game";
    }

    @Override
    public String getArguments() {
        return "<id> [player]";
    }

    @Override
    public boolean canConsoleUse() {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean execute(CommandSender sender, String[] args) {
        if(args.length > 2)
            return false;
        if(args.length != 2 && sender instanceof ConsoleCommandSender){
            sender.sendMessage(ChatColor.RED + "Cannot run command without arguments from the console!");
            return true;
        }
        else if(sender instanceof Player){
            Player p;
            if(args.length == 0)
                p = (Player) sender;
            else
                p = Bukkit.getPlayer(args[1]);
            Location l = null;
            for(Location location : plugin.signManager.getJoinSigns().keySet()){
                if(plugin.signManager.getJoinSigns().get(location).getGame().getArena() == plugin.arenaManager.getArena(args[0]))
                    l = location;
            }
            if(l == null){
                sender.sendMessage(ChatColor.RED+"The requested arena cannot be found!");
                return true;
            }
            PlayerInteractEvent event = new PlayerInteractEvent(p, Action.RIGHT_CLICK_BLOCK, new ItemStack(Material.AIR), l.getBlock(), BlockFace.SELF);
            plugin.getServer().getPluginManager().callEvent(event);//Should trigger the player interact event inside SignManager
        }
        return true;
    }
}
