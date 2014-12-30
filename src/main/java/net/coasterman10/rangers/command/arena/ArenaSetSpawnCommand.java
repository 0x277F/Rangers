package net.coasterman10.rangers.command.arena;

import net.coasterman10.rangers.SpawnVector;
import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.command.Subcommand;
import net.coasterman10.rangers.game.RangersTeam;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaSetSpawnCommand implements Subcommand {
    private final ArenaManager arenaManager;

    public ArenaSetSpawnCommand(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public String getName() {
        return "setspawn";
    }

    @Override
    public String getDescription() {
        return "Sets spawnpoints for an arena";
    }

    @Override
    public String getArguments() {
        return ChatColor.GREEN + "<id> <rangers|bandits|lobby|spectators>";
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
                int team = -1; // RANGERS = 0, BANDITS = 1, LOBBY = 2, SPECTATORS = 3
                if (args[1].equalsIgnoreCase("rangers"))
                    team = 0;
                else if (args[1].equalsIgnoreCase("bandits"))
                    team = 1;
                else if (args[1].equalsIgnoreCase("lobby"))
                    team = 2;
                else if (args[1].equalsIgnoreCase("spectators"))
                    team = 3;
                else
                    return false;

                SpawnVector spawnVector = new SpawnVector(((Player) sender).getLocation());
                spawnVector.round();
                Location spawn = spawnVector.toLocation(((Player) sender).getWorld());

                String teamName = null;
                switch (team) {
                case 0:
                    a.setSpawn(RangersTeam.RANGERS, spawn);
                    teamName = "Rangers";
                    break;
                case 1:
                    a.setSpawn(RangersTeam.BANDITS, spawn);
                    teamName = "Bandits";
                    break;
                case 2:
                    a.setLobbySpawn(spawn);
                    sender.sendMessage(ChatColor.GREEN + "Set lobby spawn for arena \"" + a.getName() + "\" to "
                            + ChatColor.AQUA + "[(" + spawn.getX() + "," + spawn.getY() + "," + spawn.getZ() + "), ("
                            + spawn.getYaw() + "," + spawn.getPitch() + ")]");
                    return true;
                case 3:
                    a.setSpectatorSpawn(spawn);
                    teamName = "spectators";
                    break;
                }

                sender.sendMessage(ChatColor.GREEN + "Set spawn of arena \"" + a.getName() + "\" for " + ChatColor.AQUA
                        + teamName + ChatColor.GREEN + " to " + ChatColor.AQUA + "[(" + spawn.getX() + ","
                        + spawn.getY() + "," + spawn.getZ() + "), (" + spawn.getYaw() + "," + spawn.getPitch() + ")]");
                a.save();
            } else {
                sender.sendMessage(ChatColor.RED + "No such arena \"" + ChatColor.GOLD + args[0] + ChatColor.RED + "\"");
            }
            return true;
        }
    }
}
