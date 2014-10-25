package net.coasterman10.rangers.boss;

import net.coasterman10.rangers.command.Subcommand;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnBossSubcommand implements Subcommand {

    @Override
    public String getName() {
        return "spawnboss";
    }

    @Override
    public String getDescription() {
        return "DEBUG - Spawn the boss mob at your location";
    }

    @Override
    public String getArguments() {
        return null;
    }

    @Override
    public boolean canConsoleUse() {
        return false;
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] args) {
        if(commandSender instanceof Player && commandSender.hasPermission("rangers.debug.boss")){
            try {
                EntityTypes.spawnEntity(EntityTypes.GOLEM_BOSS, ((Player) commandSender).getLocation());
            } catch (Exception e){
                commandSender.sendMessage(ChatColor.RED + e.getMessage());
            }
        } else
            commandSender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
        return true;
    }
}
