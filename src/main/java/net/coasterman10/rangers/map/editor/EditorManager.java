package net.coasterman10.rangers.map.editor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.game.GameTeam;
import net.coasterman10.rangers.map.GameMap;
import net.coasterman10.rangers.map.GameMapManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class EditorManager implements Listener, CommandExecutor {
    private final Rangers plugin;
    private final GameMapManager maps;

    private Map<UUID, EditorSession> sessions = new HashMap<>();

    public EditorManager(Rangers plugin, GameMapManager maps) {
        this.plugin = plugin;
        this.maps = maps;
    }

    public void openEditor(Player player, GameMap map) {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("bounds")) {

                }
                if (args[0].equalsIgnoreCase("create")) {

                }
                if (args[0].equalsIgnoreCase("edit")) {
                    if (args.length >= 2) {
                        if (!sessions.containsKey(player.getUniqueId())) {
                            GameMap map = maps.getMap(args[1]);
                            if (map != null) {
                                player.sendMessage(ChatColor.GREEN + "Opening map \"" + args[1] + "\"...");
                                World world = new WorldCreator("editor-" + player.getUniqueId()).createWorld();
                                Location origin = new Location(world, 0, 64, 0);
                                map.getSchematic().buildDelayed(origin, plugin);
                                player.teleport(map.getSpawn(GameTeam.SPECTATORS).addTo(origin));
                                sessions.put(player.getUniqueId(), new EditorSession(player, map, origin));
                            } else {
                                player.sendMessage(ChatColor.RED + "Map \"" + args[1] + "\" does not exist.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You are already editing a map.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/map edit <name>");
                    }
                }
                if (args[0].equalsIgnoreCase("close")) {
                    if (sessions.containsKey(player.getUniqueId())) {
                        final EditorSession s = sessions.get(player.getUniqueId());
                        player.sendMessage(ChatColor.GREEN + "Closing editor for map \"" + s.getMap().name + "\"");
                        player.teleport(plugin.getLobbySpawn());
                        Bukkit.unloadWorld(s.getOrigin().getWorld(), false);
                        final String worldName = s.getOrigin().getWorld().getName();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                recursiveDelete(new File(Bukkit.getWorldContainer(), worldName));
                            }

                            private void recursiveDelete(File file) {
                                if (file.isDirectory())
                                    for (File f : file.listFiles())
                                        recursiveDelete(f);
                                else
                                    file.delete();
                            }
                        }.runTaskAsynchronously(plugin);
                        sessions.remove(player.getUniqueId());
                    }
                }
                if (args[0].equalsIgnoreCase("setspawn")) {
                    if (args.length >= 2) {
                        if (sessions.containsKey(player.getUniqueId())) {
                            EditorSession s = sessions.get(player.getUniqueId());
                            boolean changed = false;
                            if (args[1].equalsIgnoreCase("lobby")) {
                                s.getMap().setLobbySpawn(s.getVectorizedLocation());
                                changed = true;
                            } else {
                                GameTeam team = GameTeam.valueOf(args[1].toLowerCase());
                                if (team != null) {
                                    s.getMap().setSpawn(team, s.getVectorizedLocation());
                                    changed = true;
                                } else {
                                    player.sendMessage(ChatColor.RED + "\"" + args[1] + "\" is not a valid team.");
                                }
                            }
                            if (changed) {
                                maps.saveMap(s.getMap());
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You are not currently editing a map.");
                        }
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append(ChatColor.RED).append("Usage: ").append(ChatColor.YELLOW).append("/map setspawn <");
                        for (GameTeam team : GameTeam.values())
                            sb.append(ChatColor.AQUA).append(team.name().toLowerCase()).append(ChatColor.YELLOW)
                                    .append("|");
                        sb.append(ChatColor.AQUA).append("lobby").append(ChatColor.YELLOW);
                        sb.append(">");
                    }
                }
            }
        } else {
            sender.sendMessage("Only players can use the editor");
        }
        return true;
    }
}
