package net.coasterman10.rangers.command;

import net.coasterman10.rangers.Rangers;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RangersReloadSubcommand implements Subcommand {
    private final Rangers plugin;

    public RangersReloadSubcommand(Rangers plugin) {
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
    public boolean canConsoleUse() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args, Object[] data) {
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
    }
}
