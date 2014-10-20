package net.coasterman10.rangers.command;

import org.bukkit.command.CommandSender;

public interface Subcommand {
    public String getName();
    
    public String getDescription();
    
    public String getArguments();
    
    public boolean canConsoleUse();
    
    public boolean execute(CommandSender sender, String[] args);
}
