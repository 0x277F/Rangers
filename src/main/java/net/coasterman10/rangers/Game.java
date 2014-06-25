package net.coasterman10.rangers;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class Game {
    public static final int MIN_PLAYERS = 4;
    public static final int MAX_PLAYERS = 10;

    private static int nextId;

    public static int getNextId() {
        return nextId;
    }

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

    public Game(Rangers plugin, GameSign sign, GameMap map, Location lobby, Location arena) {
        id = nextId++;

        Validate.notNull(sign);
        Validate.notNull(map);
        this.plugin = plugin;
        this.map = map;
        this.sign = sign;
        this.lobby = lobby;
        this.arena = arena;

        sign.setGame(this);
        sign.setPlayers(0);
        sign.setMapName(map.name);
        sign.setStatusMessage("In Lobby");
    }

    public int getId() {
        return id;
    }

    public boolean addPlayer(UUID id) {
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

    public GameMap getMap() {
        return map;
    }
}
