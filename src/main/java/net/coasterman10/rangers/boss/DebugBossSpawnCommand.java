package net.coasterman10.rangers.boss;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugBossSpawnCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player && commandSender.hasPermission("permissions.manage")){//Only PEX admins can spawn them
            EntityTypes.spawnEntity(EntityTypes.GOLEM_BOSS, ((Player) commandSender).getLocation());
        }
        return true;
    }
}
