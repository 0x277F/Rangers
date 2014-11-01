package net.coasterman10.rangers.command.rangers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import net.coasterman10.rangers.command.Subcommand;
import net.coasterman10.rangers.game.GameSettings;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RangersSettingCommand implements Subcommand {
    private GameSettings settings;

    private Map<String, Field> fields = new HashMap<>();

    public RangersSettingCommand(GameSettings settings) {
        this.settings = settings;

        for (Field field : settings.getClass().getFields()) {
            if (!Modifier.isFinal(field.getModifiers())) {
                fields.put(field.getName().toLowerCase(), field);
            }
        }
    }

    @Override
    public String getName() {
        return "setting";
    }

    @Override
    public String getDescription() {
        return "Display or set game settings";
    }

    @Override
    public String getArguments() {
        return ChatColor.GREEN + "<setting|list>" + ChatColor.BLUE + " [value]";
    }
    
    @Override
    public String getPermission() {
        return "rangers.settings";
    }

    @Override
    public boolean canConsoleUse() {
        return true;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        } else if (args[0].equalsIgnoreCase("list")) {
            listSettings(sender);
        } else {
            Field field = fields.get(args[0].toLowerCase());
            if (field != null) {
                if (args.length == 1) {
                    displayField(sender, field);
                } else if (args.length == 2) {
                    setField(sender, field, args[1]);
                }
            } else {
                sender.sendMessage(ChatColor.RED + "No such game setting \"" + ChatColor.GOLD + args[0] + ChatColor.RED
                        + "\"");
            }
        }
        return true;
    }

    private void listSettings(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Game settings: ");
        for (Field field : fields.values()) {
            try {
                sender.sendMessage(ChatColor.GOLD + "- " + ChatColor.YELLOW + field.getName() + ChatColor.GOLD + " = "
                        + ChatColor.AQUA + field.get(settings));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                sender.sendMessage(ChatColor.RED + "Failed to get " + ChatColor.GOLD + field.getName());
            }
        }
    }

    private void displayField(CommandSender sender, Field field) {
        try {
            sender.sendMessage(ChatColor.GOLD + "Setting " + ChatColor.YELLOW + field.getName() + ChatColor.GOLD
                    + " = " + ChatColor.AQUA + field.get(settings));
        } catch (IllegalArgumentException | IllegalAccessException e) {
            sender.sendMessage(ChatColor.RED + "Failed to get " + ChatColor.GOLD + field.getName());
        }
    }

    private void setField(CommandSender sender, Field field, String value) {
        // Parse value given. If other variable types need be added in the future, add an if statement
        // for that type.
        Class<?> c = field.getType();
        if (c.isAssignableFrom(int.class)) {
            try {
                int i = Integer.parseInt(value);
                try {
                    field.set(settings, i);
                    sender.sendMessage(ChatColor.GREEN + "Updated " + ChatColor.YELLOW + field.getName()
                            + ChatColor.GOLD + " = " + ChatColor.GREEN + field.get(settings));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    sender.sendMessage(ChatColor.RED + "Failed to set " + ChatColor.GOLD + field.getName());
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "\"" + ChatColor.GOLD + value + ChatColor.RED
                        + "\" is not an integer");
            }
        }
    }
}
