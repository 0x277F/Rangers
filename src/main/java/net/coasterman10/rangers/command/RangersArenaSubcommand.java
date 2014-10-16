package net.coasterman10.rangers.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RangersArenaSubcommand extends SubcommandExecutor implements Subcommand {
    public RangersArenaSubcommand() {
        super("arena");
    }

    @Override
    public String getName() {
        return "arena";
    }

    @Override
    public String getDescription() {
        return "Configure game arenas";
    }

    @Override
    public String getArguments() {
        return ChatColor.BLUE + "[...]";
    }

    @Override
    public boolean canConsoleUse() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        onCommand(sender, null, "arena", args);
    }
}
