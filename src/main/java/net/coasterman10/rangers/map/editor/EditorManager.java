package net.coasterman10.rangers.map.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.coasterman10.rangers.EmptyChunkGenerator;
import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.game.GameTeam;
import net.coasterman10.rangers.map.GameMap;
import net.coasterman10.rangers.map.GameMapManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

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
                if (args[0].equalsIgnoreCase("list")) {
                    player.sendMessage(ChatColor.YELLOW + "Maps:");
                    for (GameMap map : maps.getMaps())
                        player.sendMessage(ChatColor.YELLOW + "- " + ChatColor.AQUA + map.name);
                }
                if (args[0].equalsIgnoreCase("create")) {

                }
                if (args[0].equalsIgnoreCase("edit")) {
                    if (args.length >= 2) {
                        if (!sessions.containsKey(player.getUniqueId())) {
                            GameMap map = maps.getMap(args[1]);
                            if (map != null) {
                                World world = new WorldCreator("editor-" + player.getUniqueId()).generator(
                                        new EmptyChunkGenerator()).createWorld();
                                Location origin = new Location(world, 0, 64, 0);
                                EditorSession session = new EditorSession(plugin, player, map, origin);
                                session.load();
                                sessions.put(player.getUniqueId(), session);
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
                        sessions.get(player.getUniqueId()).unload();
                        sessions.remove(player.getUniqueId());
                    }
                }
                if (args[0].equalsIgnoreCase("setspawn")) {
                    if (args.length >= 2) {
                        if (sessions.containsKey(player.getUniqueId())) {
                            EditorSession session = sessions.get(player.getUniqueId());
                            if (args[1].equalsIgnoreCase("lobby")) {
                                session.setLobbySpawn();
                            } else {
                                try {
                                    GameTeam team = GameTeam.valueOf(args[1].toUpperCase());
                                    session.setSpawn(team);
                                } catch (IllegalArgumentException e) {
                                    // TODO Less hacky solution
                                    player.sendMessage(ChatColor.RED + "\"" + args[1] + "\" is not a valid team.");
                                    return true;
                                }
                            }
                            maps.saveMap(session.getMap());
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
                        player.sendMessage(sb.toString());
                    }
                }
            }
        } else {
            sender.sendMessage("Only players can use the editor");
        }
        return true;
    }
}
