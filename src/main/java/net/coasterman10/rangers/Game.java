package net.coasterman10.rangers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import me.confuser.barapi.BarAPI;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class Game {
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 10;
    public static final int COUNTDOWN_DURATION = 60;
    public static final int LOCK_TIME = 50;
    public static final int TEAM_SELECT_TIME = 50;
    public static final int RESTART_TIME = 10;

    private static int nextId;

    private final int id;
    private final Rangers plugin;
    private Arena arena;

    private GameScoreboard scoreboard;
    private GameSign sign;

    // If you are going to give me hell about using 3 collections, please stop using your grandmother's 90s PC
    private Collection<UUID> players = new HashSet<>();
    private Collection<UUID> bandits = new HashSet<>();
    private Collection<UUID> rangers = new HashSet<>();
    private UUID banditLeader;

    private State state;
    private int seconds;

    public Game(Rangers plugin, GameSign sign) {
        id = nextId++;

        Validate.notNull(sign);
        this.plugin = plugin;
        this.sign = sign;

        scoreboard = new GameScoreboard();

        sign.setGame(this);
        sign.setPlayers(players.size());
        sign.setStatusMessage("Waiting for Arena");
        sign.setMapName("N / A");

        state = State.INACTIVE;

        new UpdateTask().runTaskTimer(plugin, 0L, 20L);
    }

    public void setArena(Arena arena) {
        this.arena = arena;
        sign.setMapName(arena.getMapName());
        sign.setStatusMessage("In Lobby");
        state = State.LOBBY;
    }

    public int getId() {
        return id;
    }

    public void addPlayer(Player player) {
        if (state == State.INACTIVE) {
            player.sendMessage(ChatColor.RED + "That game is not set up correctly, please notify an administrator.");
            return;
        }

        if (state == State.LOBBY || (state == State.STARTING && seconds > LOCK_TIME)) {
            if (players.size() == MAX_PLAYERS) {
                player.sendMessage(ChatColor.RED + "This game is full!");
                return;
            }
            players.add(player.getUniqueId());
            plugin.getPlayerData(player).setGame(this);
            player.teleport(arena.getLobbySpawn());
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20F);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            broadcast(ChatColor.YELLOW + player.getName() + ChatColor.AQUA + " joined the game");
            sign.setPlayers(players.size());
        } else {
            player.sendMessage(ChatColor.RED + "You cannot join this game once it is in progress!");
        }
    }

    public void removePlayer(Player player) {
        UUID id = player.getUniqueId();
        if (!players.contains(id))
            return;
        players.remove(id);
        bandits.remove(id);
        rangers.remove(id);
        sign.setPlayers(players.size());
        BarAPI.removeBar(player);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    private void broadcast(String msg) {
        for (UUID id : players) {
            Bukkit.getPlayer(id).sendMessage(msg);
        }
    }

    private void selectTeams() {
        boolean team = false;
        List<UUID> ids = new ArrayList<>(players);
        Collections.shuffle(ids);
        for (UUID id : ids) {
            if (team) {
                plugin.getPlayerData(id).setTeam(GameTeam.RANGERS);
                rangers.add(id);
                Bukkit.getPlayer(id).sendMessage(
                        ChatColor.AQUA + "You have been selected to join the " + ChatColor.GREEN + "RANGERS");
            } else {
                plugin.getPlayerData(id).setTeam(GameTeam.BANDITS);
                bandits.add(id);
                Bukkit.getPlayer(id).sendMessage(
                        ChatColor.AQUA + "You have been selected to join the " + ChatColor.RED + "BANDITS");
                if (banditLeader == null) {
                    banditLeader = id;
                    Bukkit.getPlayer(id).sendMessage(ChatColor.AQUA + "You are the " + ChatColor.RED + "Bandit Leader");
                }
            }
            team = !team;
        }

        for (Player ranger : rangers()) {
            scoreboard.setTeam(ranger, GameTeam.RANGERS);
            ranger.setScoreboard(scoreboard.getScoreboard(GameTeam.RANGERS));
        }
        for (Player bandit : bandits()) {
            scoreboard.setTeam(bandit, GameTeam.BANDITS);
            bandit.setScoreboard(scoreboard.getScoreboard(GameTeam.BANDITS));
        }
        scoreboard.setBanditLeader(Bukkit.getPlayer(banditLeader));
    }

    private void checkChest(GameTeam t) {
        Location loc = (t == GameTeam.RANGERS ? arena.getRangerChest() : arena.getBanditChest());
        if (loc.getBlock().getState() instanceof Chest) {
            Chest state = (Chest) loc.getBlock().getState();
            for (ItemStack item : state.getBlockInventory()) {
                if (item != null && item.getType() == Material.SKULL_ITEM) {
                    SkullMeta meta = (SkullMeta) item.getItemMeta();
                    if (meta.hasOwner()) {
                        for (Player p : (t == GameTeam.RANGERS ? bandits() : rangers())) {
                            if (meta.getOwner().equals(p.getName())) {
                                scoreboard.setScore(t == GameTeam.RANGERS ? "Rangers" : "Bandits",
                                        scoreboard.getScore(t == GameTeam.RANGERS ? "Rangers" : "Bandits") + 1);
                            }
                        }
                        if (Bukkit.getPlayer(banditLeader).getName().equals(meta.getOwner())) {
                            scoreboard.setScore("Bandit Leader", scoreboard.getScore("Bandit Leader") + 1);
                        }
                    }
                }
            }
        }
    }

    private void setState(State state) {
        this.state = state;
        state.start(this);
        state.onSecond(this);
    }

    private class UpdateTask extends BukkitRunnable {
        @Override
        public void run() {
            state.onSecond(Game.this);
        }
    }

    private enum State {
        INACTIVE {
            @Override
            public void start(final Game g) {

            }

            @Override
            public void onSecond(final Game g) {

            }
        },
        LOBBY {
            @Override
            public void start(final Game g) {

            }

            @Override
            public void onSecond(final Game g) {
                for (Player p : g.players()) {
                    BarAPI.setMessage(p, ChatColor.GREEN + "" + ChatColor.BOLD + "Rangers " + ChatColor.BLUE
                            + ChatColor.BOLD + "ALPHA" + ChatColor.GRAY + " | " + ChatColor.AQUA + "69.137.10.168", 1F);
                }
                if (g.players.size() >= MIN_PLAYERS) {
                    g.setState(State.STARTING);
                }
            }
        },
        STARTING {
            @Override
            public void start(final Game g) {
                g.seconds = COUNTDOWN_DURATION;
            }

            @Override
            public void onSecond(final Game g) {
                g.sign.setStatusMessage("Starting in " + g.seconds);
                if (g.seconds == 0) {
                    g.setState(RUNNING);
                } else {
                    if (g.players().size() < MIN_PLAYERS) {
                        g.setState(LOBBY);
                    }
                    if (g.seconds == TEAM_SELECT_TIME) {
                        g.selectTeams();
                    }
                    float percent = (float) g.seconds / (float) COUNTDOWN_DURATION;
                    for (Player p : g.players()) {
                        BarAPI.setMessage(p, ChatColor.GREEN + "Starting in " + g.seconds, percent);
                    }
                    g.seconds--;
                }
            }
        },
        RUNNING {
            @Override
            public void start(Game g) {
                g.scoreboard.setScore("Bandits", 0);
                g.scoreboard.setScore("Rangers", 0);
                g.scoreboard.setScore("Bandit Leader", 0);
                for (Player p : g.players()) {
                    BarAPI.setMessage(p, ChatColor.GREEN + "" + ChatColor.BOLD + "Rangers " + ChatColor.BLUE
                            + ChatColor.BOLD + "ALPHA" + ChatColor.GRAY + " | " + ChatColor.AQUA + "69.137.10.168", 1F);
                    g.plugin.getPlayerData(p).setAlive(true);
                }
                for (Player p : g.rangers()) {
                    p.teleport(g.arena.getRangerSpawn());
                }
                for (Player p : g.bandits()) {
                    p.teleport(g.arena.getBanditSpawn());
                }
            }

            @Override
            public void onSecond(Game g) {
                g.checkChest(GameTeam.RANGERS);
                g.checkChest(GameTeam.BANDITS);
                
                // Check victory conditions
                if (!g.plugin.getPlayerData(g.banditLeader).isAlive()) {
                    g.setState(ENDING);
                    g.broadcast(ChatColor.RED + "The Bandit Leader has been defeated!");
                    g.broadcast(ChatColor.GREEN + "" + ChatColor.BOLD + "THE RANGERS WIN!");
                } else {
                    // If all the rangers are dead, the bandits have won
                    boolean rangerAlive = false;
                    for (UUID ranger : g.rangers)
                        if (g.plugin.getPlayerData(ranger).isAlive())
                            rangerAlive = true;
                    if (!rangerAlive) {
                        g.setState(ENDING);
                        g.broadcast(ChatColor.RED + "The rangers have been defeated!");
                        g.broadcast(ChatColor.GREEN + "" + ChatColor.BOLD + "THE BANDITS WIN!");
                    }
                }
            }
        },
        ENDING {
            @Override
            public void start(Game g) {
                g.seconds = RESTART_TIME;
            }

            @Override
            public void onSecond(Game g) {
                if (g.seconds == 0) {
                    g.setState(LOBBY);
                    for (Player p : g.players()) {
                        BarAPI.removeBar(p);
                        p.teleport(g.arena.getLobbySpawn());
                        g.scoreboard.setTeam(p, null);
                    }
                } else {
                    for (Player p : g.players()) {
                        BarAPI.setMessage(p, ChatColor.GREEN + "Restarting in " + g.seconds);
                    }
                    g.seconds--;
                }
            }
        };

        public abstract void start(final Game g);

        public abstract void onSecond(final Game g);
    }

    private Collection<Player> players() {
        Collection<Player> collection = new ArrayList<>();
        for (UUID id : players)
            collection.add(Bukkit.getPlayer(id));
        return collection;
    }

    private Collection<Player> rangers() {
        Collection<Player> collection = new ArrayList<>();
        for (UUID id : players)
            collection.add(Bukkit.getPlayer(id));
        return collection;
    }

    private Collection<Player> bandits() {
        Collection<Player> collection = new ArrayList<>();
        for (UUID id : bandits)
            collection.add(Bukkit.getPlayer(id));
        return collection;
    }

    public Location getLobbySpawn() {
        return arena.getLobbySpawn();
    }

    public boolean allowPvp() {
        return state == State.RUNNING;
    }
}
