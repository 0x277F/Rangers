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
        return ChatColor.GRAY + "(" + ChatColor.GREEN + "<add|remove|list>" + ChatColor.GRAY + "/" + ChatColor.GREEN
                + "<id> " + ChatColor.BLUE + "[...]" + ChatColor.GRAY + ")";
    }

    @Override
    public boolean canConsoleUse() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args, Object[] data) {
        onCommand(sender, null, label, args);
    }
}
