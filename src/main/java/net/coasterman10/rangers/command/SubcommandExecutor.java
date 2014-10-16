package net.coasterman10.rangers.command;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubcommandExecutor implements CommandExecutor {
    private final String name;

    private Map<String, Subcommand> subcommands = new LinkedHashMap<>();

    public SubcommandExecutor(String name) {
        this.name = name;
    }

    public void registerSubcommand(Subcommand subcommand) {
        subcommands.put(subcommand.getName().toLowerCase(), subcommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
        } else {
            String subcommandLabel = args[0].toLowerCase();
            if (subcommands.containsKey(subcommandLabel)) {
                Subcommand subcommand = subcommands.get(subcommandLabel);
                if (subcommand.canConsoleUse() || sender instanceof Player) {
                    subcommands.get(subcommandLabel).execute(sender, Arrays.copyOfRange(args, 1, args.length));
                } else {
                    sender.sendMessage("Only players can use that command");
                }
            } else {
                showHelp(sender);
            }
        }
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Help for " + ChatColor.YELLOW + "/" + name);
        for (Subcommand subcommand : subcommands.values()) {
            sender.sendMessage(ChatColor.GOLD + "/" + name + " " + ChatColor.YELLOW + subcommand.getName()
                    + ChatColor.WHITE + ": " + subcommand.getDescription());
        }
    }
}
