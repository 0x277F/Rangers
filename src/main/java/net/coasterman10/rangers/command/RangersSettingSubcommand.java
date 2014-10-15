package net.coasterman10.rangers.command;

import net.coasterman10.rangers.game.GameSettings;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RangersSettingSubcommand implements Subcommand {
    private GameSettings settings;
    
    public RangersSettingSubcommand(GameSettings settings) {
        this.settings = settings;
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
        return ChatColor.GREEN + "<setting>" + ChatColor.BLUE + " [value]";
    }

    @Override
    public boolean canConsoleUse() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Available settings:");
            
        } else {
            
        }
    }
}
