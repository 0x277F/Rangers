package net.coasterman10.rangers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import me.confuser.barapi.BarAPI;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class Game {
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 10;
    public static final int COUNTDOWN_DURATION = 30;

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

    private State state;
    private int seconds;

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

        state = State.INACTIVE;

        new UpdateTask().runTaskTimer(plugin, 0L, 20L);
    }

    public void setArena(Arena a) {
        lobby = a.getLobbyLocation();
        arena = a.getArenaLocation();
        statusMessage = "In Lobby";
        state = State.LOBBY;
    }

    public int getId() {
        return id;
    }

    public boolean addPlayer(UUID id) {
        if (state == State.INACTIVE) {
            Bukkit.getPlayer(id).sendMessage(
                    ChatColor.RED + "That game is not set up correctly, please notify an administrator.");
            return true;
        }

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

    public GameMap getMap() {
        return map;
    }

    private class UpdateTask extends BukkitRunnable {
        @Override
        public void run() {
            // States are programmed assuming the run() function is called immediately when the state starts.
            // Thus, if the state changes, the next state needs to be run. This should continue until the state
            // is not changed any longer, hence the do-while loop.
            State old;
            do {
                old = state;
                state.run(Game.this);
            } while (state != old);
            sign.setPlayers(players.size());
            sign.setMapName(map.name);
            sign.setStatusMessage(statusMessage);
        }
    }

    private enum State {
        INACTIVE {
            @Override
            public void run(final Game g) {

            }
        },
        LOBBY {
            @Override
            public void run(final Game g) {
                if (g.players.size() >= MIN_PLAYERS) {
                    g.state = STARTING;
                    g.seconds = COUNTDOWN_DURATION;
                }
            }
        },
        STARTING {
            @Override
            public void run(final Game g) {
                g.statusMessage = "Starting in " + g.seconds;
                if (g.seconds == 0) {
                    for (Player p : g.players())
                        BarAPI.removeBar(p);
                    g.state = State.RUNNING;
                } else {
                    for (Player p : g.players())
                        BarAPI.setMessage(p, ChatColor.GREEN + "Starting in " + g.seconds, g.seconds
                                / (float) COUNTDOWN_DURATION);
                }
                g.seconds--;
            }
        },
        RUNNING {
            @Override
            public void run(final Game g) {

            }
        },
        ENDING {
            @Override
            public void run(final Game g) {

            }
        };

        public abstract void run(final Game g);
    }

    private Collection<Player> players() {
        Collection<Player> collection = new ArrayList<Player>();
        for (UUID id : players)
            collection.add(Bukkit.getPlayer(id));
        return collection;
    }
}
