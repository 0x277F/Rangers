package net.coasterman10.rangers.command;

import net.coasterman10.rangers.arena.ArenaManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RangersArenaConfigCommand extends SubcommandExecutor implements Subcommand {
    private final ArenaManager arenaManager;

    public RangersArenaConfigCommand(ArenaManager arenaManager) {
        super(SubcommandExecutor.ANY_STRING);
        this.arenaManager = arenaManager;
    }

    @Override
    public String getName() {
        return SubcommandExecutor.ANY_STRING;
    }

    @Override
    public String getDescription() {
        return "Configures arena settings";
    }

    @Override
    public String getArguments() {
        return ChatColor.GREEN + "<id> " + ChatColor.BLUE + "[...]";
    }

    @Override
    public boolean canConsoleUse() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args, Object[] data) {
        if (arenaManager.getArena(label) != null) {
            onCommand(sender, null, label, args);
        } else {
            sender.sendMessage(ChatColor.RED + "No such arena \"" + label + "\"");
        }
    }

    @Override
    protected Object[] getData(String label, String[] args) {
        return new Object[] { arenaManager.getArena(label) };
    }
}
