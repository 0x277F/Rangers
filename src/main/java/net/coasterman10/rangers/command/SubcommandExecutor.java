package net.coasterman10.rangers.command;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A CommandExecutor implementation that uses the first argument to denote another command in a hierarchical manner.
 */
public class SubcommandExecutor implements CommandExecutor {
    private final String name;

    private Map<String, Subcommand> subcommands = new LinkedHashMap<>();

    public SubcommandExecutor(String name) {
        this.name = name;
    }

    /**
     * Registers a subcommand. If another subcommand with the same name has already been registered, it will be
     * replaced.
     * 
     * @param subcommand The subcommand to register.
     */
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

                // If the subcommand cannot be used by the console, check that the sender is a player. It is guaranteed
                // not to execute if the sender is console but the subcommand does not allow console to run it.
                if (subcommand.canConsoleUse() || sender instanceof Player) {
                    // The first argument is the label, so shift off the first argument to the left.
                    String[] subcommandArgs = Arrays.copyOfRange(args, 1, args.length);

                    // Check that the user has permissions to execute the subcommand.
                    if (subcommand.getPermission() == null || sender.hasPermission(subcommand.getPermission())) {
                        // Execute the subcommand and display usage if the user did not enter the correct arguments.
                        if (!subcommand.execute(sender, subcommandArgs)) {
                            sender.sendMessage(ChatColor.GOLD + subcommand.getDescription());
                            sender.sendMessage(ChatColor.GOLD + "/" + name + " " + ChatColor.YELLOW
                                    + subcommand.getName() + ChatColor.GOLD + " " + subcommand.getArguments());
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to execute that command.");
                    }
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
