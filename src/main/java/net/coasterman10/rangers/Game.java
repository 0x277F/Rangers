package net.coasterman10.rangers;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

public class Game {
    public static final int MIN_PLAYERS = 4;
    public static final int MAX_PLAYERS = 10;

    private static int nextId;

    private final int id;
    private final Rangers plugin;
    private final GameMap map;

    private Scoreboard scoreboard;
    private Objective kills;

    private GameSign sign;
    private Location lobby;
    private Location arena;

    // If you are going to give me hell about using 3 collections, please stop using your grandmother's 90s PC
    private Collection<UUID> players = new HashSet<>();
    private Collection<UUID> bandits = new HashSet<>();
    private Collection<UUID> rangers = new HashSet<>();
    private UUID banditLeader;

    private String statusMessage;

    public Game(Rangers plugin, GameSign sign, GameMap map) {
        id = nextId++;

        Validate.notNull(sign);
        Validate.notNull(map);
        this.plugin = plugin;
        this.map = map;
        this.sign = sign;

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        kills = scoreboard.registerNewObjective("obj", "dummy");
        kills.setDisplayName(ChatColor.GOLD + "Heads Collected");
        kills.setDisplaySlot(DisplaySlot.SIDEBAR);
        kills.getScore("Rangers").setScore(0);
        kills.getScore("Bandits").setScore(0);
        kills.getScore("Bandit Leader").setScore(0);

        sign.setGame(this);
        statusMessage = "Waiting for Arena";

        new UpdateTask().runTaskTimer(plugin, 0L, 20L);
    }

    public void setArena(Arena a) {
        lobby = a.getLobbyLocation();
        arena = a.getArenaLocation();
        statusMessage = "In Lobby";
    }

    public int getId() {
        return id;
    }

    public boolean addPlayer(UUID id) {
        if (lobby == null || arena == null)
            throw new IllegalStateException("Game does not yet have an arena");

        if (players.size() == MAX_PLAYERS)
            return false;
        players.add(id);
        Player p = Bukkit.getPlayer(id);
        plugin.getPlayerData(id).setGame(this);
        p.teleport(lobby.clone().add(map.lobbySpawn));
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.setSaturation(20F);
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        p.setScoreboard(scoreboard);
        broadcast(ChatColor.YELLOW + p.getName() + ChatColor.AQUA + " joined the game");
        return true;
    }

    public void removePlayer(UUID id) {
        if (!players.contains(id))
            return;
        players.remove(id);
        bandits.remove(id);
        rangers.remove(id);
    }

    private void broadcast(String msg) {
        for (UUID id : players) {
            Bukkit.getPlayer(id).sendMessage(msg);
        }
    }

    public boolean hasHopper(Location loc) {
        return loc.equals(lobby.clone().add(map.rangerHopper)) || loc.equals(lobby.clone().add(map.banditHopper));
    }

    public boolean checkHopper(Location loc, ItemStack item) {
        Vector pos = loc.clone().subtract(lobby).toVector();
        String owner = ((SkullMeta) item.getItemMeta()).getOwner();
        @SuppressWarnings("deprecation") // Bloody hell, Bukkit. This shouldn't be deprecated at all!
        UUID id = Bukkit.getOfflinePlayer(owner).getUniqueId();
        if (pos.equals(map.rangerHopper)) {
            if (bandits.contains(id)) {
                // TODO Logic
                if (banditLeader.equals(id)) {
                    // TODO Logic
                }
            } else {
                return false;
            }
        }
        if (pos.equals(map.banditHopper)) {
            if (rangers.contains(id)) {
                // TODO Logic
            } else {
                return false;
            }
        }

        return true;
    }

    public GameMap getMap() {
        return map;
    }

    private class UpdateTask extends BukkitRunnable {
        @Override
        public void run() {
            sign.setPlayers(players.size());
            sign.setMapName(map.name);
            sign.setStatusMessage(statusMessage);
        }
    }
}
