package net.coasterman10.rangers.command;

import org.bukkit.command.CommandSender;

public interface Subcommand {
    public String getName();
    
    public String getDescription();
    
    public String getArguments();
    
    public boolean canConsoleUse();
    
    public void execute(CommandSender sender, String label, String[] args, Object[] data);
}
