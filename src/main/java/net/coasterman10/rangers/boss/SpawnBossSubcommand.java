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
    public String getPermission() {
        return "rangers.debug.boss";
    }

    @Override
    public boolean canConsoleUse() {
        return false;
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] args) {
        try {
            EntityTypes.spawnEntity(EntityTypes.GOLEM_BOSS, ((Player) commandSender).getLocation());
        } catch (Exception e) {
            commandSender.sendMessage(ChatColor.RED + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
}
