package net.coasterman10.rangers.command.rangers;

import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.command.Subcommand;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RangersReloadCommand implements Subcommand {
    private final Rangers plugin;

    public RangersReloadCommand(Rangers plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads configuration";
    }

    @Override
    public String getArguments() {
        return "";
    }
    
    @Override
    public String getPermission() {
        return "rangers.eload";
    }

    @Override
    public boolean canConsoleUse() {
        return true;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
        return true;
    }
}
