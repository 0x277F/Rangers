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
    public static final String ANY_STRING = "ANY_STRING";

    private final String name;

    private Map<String, Subcommand> subcommands = new LinkedHashMap<>();

    public SubcommandExecutor(String name) {
        this.name = name;
    }

    public void registerSubcommand(Subcommand subcommand) {
        if (subcommand.getName().isEmpty())
            subcommands.put(ANY_STRING.toLowerCase(), subcommand);
        else
            subcommands.put(subcommand.getName().toLowerCase(), subcommand);
    }

    protected boolean isSubcommandRegistered(String name) {
        return subcommands.containsKey(name.toLowerCase());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
        } else {
            Subcommand subcommand = null;
            String subcommandLabel = args[0].toLowerCase();
            if (subcommands.containsKey(subcommandLabel)) {
                subcommand = subcommands.get(subcommandLabel);
                args = Arrays.copyOfRange(args, 1, args.length);
            } else if (subcommands.containsKey(ANY_STRING)) {
                subcommand = subcommands.get(ANY_STRING);
            }
            if (subcommand != null) {
                if (subcommand.canConsoleUse() || sender instanceof Player) {
                    subcommand.execute(sender, subcommandLabel, args, getData(label, args));
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
            sender.sendMessage(ChatColor.GOLD + "/" + name + " " + ChatColor.YELLOW + subcommand.getName() + " "
                    + subcommand.getArguments());
            sender.sendMessage(ChatColor.GRAY + "  " + subcommand.getDescription());
        }
    }
    
    protected Object[] getData(String label, String[] args) {
        return new Object[0];
    }
}
