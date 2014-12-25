package net.coasterman10.rangers.command.arena;

import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.arena.ClassicArena;
import net.coasterman10.rangers.command.Subcommand;
import net.coasterman10.rangers.game.GameTeam;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaSetChestCommand implements Subcommand {
    private final ArenaManager arenaManager;

    public ArenaSetChestCommand(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public String getName() {
        return "setchest";
    }

    @Override
    public String getDescription() {
        return "Sets chests for an arena";
    }

    @Override
    public String getArguments() {
        return ChatColor.GREEN + "<id> <rangers|bandits>";
    }

    @Override
    public String getPermission() {
        return "rangers.arena.build";
    }

    @Override
    public boolean canConsoleUse() {
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        } else {
            Arena a = arenaManager.getArena(args[0]);
            if (a != null) {
                if (a instanceof ClassicArena) {
                    GameTeam team;
                    try {
                        team = GameTeam.valueOf(args[1].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // HACK
                        return false;
                    }
                    @SuppressWarnings("deprecation")
                    Location chest = ((Player) sender).getTargetBlock(null, 50).getLocation();
                    if (chest != null) {
                        ((ClassicArena) a).setChest(team, chest);
                        sender.sendMessage(ChatColor.GREEN + "Set chest of arena \"" + a.getName() + "\" for "
                                + ChatColor.AQUA + team.getName() + ChatColor.GREEN + " to " + ChatColor.YELLOW + "("
                                + chest.getX() + "," + chest.getY() + "," + chest.getZ() + ")");
                        a.save();
                    } else {
                        sender.sendMessage(ChatColor.RED + "You must be targeting a block to set the chest location.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Can only set chests for a Classic or Classic-based arena");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "No such arena \"" + ChatColor.GOLD + args[0] + ChatColor.RED + "\"");
            }
            return true;
        }
    }
}
