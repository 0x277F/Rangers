package net.coasterman10.rangers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.confuser.barapi.BarAPI;
import net.coasterman10.rangers.kits.Kit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class Game {
    private static int nextId;

    private final int id;
    private final Rangers plugin;
    private final CommonSettings settings;
    private Arena arena;

    private GameScoreboard scoreboard;
    private GameSign sign;

    // If you are going to give me hell about using 3 collections, please stop using your grandmother's 90s PC
    private Collection<GamePlayer> players = new HashSet<>();
    private Map<GameTeam, Collection<GamePlayer>> teams = new EnumMap<>(GameTeam.class);
    private GamePlayer banditLeader;

    private State state;
    private int seconds;

    public Game(Rangers plugin, GameSign sign, CommonSettings settings) {
        id = nextId++;

        this.plugin = plugin;
        this.sign = sign;
        this.settings = settings;

        teams.put(GameTeam.RANGERS, new HashSet<GamePlayer>());
        teams.put(GameTeam.BANDITS, new HashSet<GamePlayer>());

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

        if (state == State.LOBBY || (state == State.STARTING && seconds > settings.lockTime)) {
            if (players.size() == settings.maxPlayers) {
                player.sendMessage(ChatColor.RED + "This game is full!");
                return;
            }
            players.add(plugin.getPlayerData(player));
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
        for (Collection<GamePlayer> team : teams.values())
            team.remove(player);
        sign.setPlayers(players.size());
        BarAPI.removeBar(player);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    private void broadcast(String msg) {
        for (GamePlayer player : players) {
            player.getHandle().sendMessage(msg);
        }
    }

    private void selectTeams() {
        GameTeam team = GameTeam.RANGERS;
        List<GamePlayer> playerList = new ArrayList<>(players);
        Collections.shuffle(playerList);
        for (GamePlayer p : playerList) {
            p.setTeam(team);
            teams.get(team).add(p);
            p.getHandle().sendMessage(
                    ChatColor.AQUA + "You have been selected to join the " + ChatColor.YELLOW + team.name());
            if (team == GameTeam.BANDITS && banditLeader == null) {
                banditLeader = p;
                p.getHandle().sendMessage(ChatColor.AQUA + "You are the " + ChatColor.RED + "Bandit Leader");
            }
            team = team.opponent();
        }

        for (GameTeam t : GameTeam.values()) {
            for (GamePlayer p : teams.get(t)) {
                scoreboard.setTeam(p.getHandle(), team);
                p.getHandle().setScoreboard(scoreboard.getScoreboard(t));
            }
        }

        scoreboard.setBanditLeader(banditLeader.getHandle());
    }

    private void checkChest(GameTeam t) {
        Location loc = (t == GameTeam.RANGERS ? arena.getRangerChest() : arena.getBanditChest());
        if (loc.getBlock().getState() instanceof Chest) {
            Chest state = (Chest) loc.getBlock().getState();
            for (ItemStack item : state.getBlockInventory()) {
                if (item != null && item.getType() == Material.SKULL_ITEM) {
                    SkullMeta meta = (SkullMeta) item.getItemMeta();
                    if (meta.hasOwner()) {
                        // Score points for enemy heads placed in the chest
                        for (GamePlayer p : teams.get(t.opponent())) {
                            if (meta.getOwner().equals(p.getHandle().getName())) {
                                scoreboard.setScore(t.getName(), scoreboard.getScore(t.getName()) + 1);
                                state.getBlockInventory().remove(item);
                            }
                        }

                        // Remove heads from the chest's own team that don't belong in it
                        for (GamePlayer p : teams.get(t)) {
                            if (meta.getOwner().equals(p.getHandle().getName())) {
                                state.getBlockInventory().remove(item);
                                loc.getWorld().dropItemNaturally(
                                        loc.getBlock().getRelative(BlockFace.UP).getLocation(), item);
                            }
                        }

                        // Score for the bandit leader's head in this chest
                        if (banditLeader.getHandle().getName().equals(meta.getOwner())) {
                            // If the head is in the rangers chest, good. If in the bandit chest, no good, remove it.
                            if (t == GameTeam.RANGERS)
                                scoreboard.setScore("Bandit Leader", scoreboard.getScore("Bandit Leader") + 1);
                            else
                                loc.getWorld().dropItemNaturally(
                                        loc.getBlock().getRelative(BlockFace.UP).getLocation(), item);
                            state.getBlockInventory().remove(item);
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
                for (GamePlayer p : g.players) {
                    BarAPI.removeBar(p.getHandle());
                    p.getHandle().teleport(g.arena.getLobbySpawn());
                    g.scoreboard.setTeam(p.getHandle(), null);
                }
            }

            @Override
            public void onSecond(final Game g) {
                for (GamePlayer p : g.players) {
                    BarAPI.setMessage(p.getHandle(), ChatColor.GREEN + "" + ChatColor.BOLD + "Rangers " + ChatColor.BLUE
                            + ChatColor.BOLD + "ALPHA" + ChatColor.GRAY + " | " + ChatColor.AQUA + "69.137.10.168", 1F);
                }
                if (g.players.size() >= g.settings.minPlayers) {
                    g.setState(State.STARTING);
                }
            }
        },
        STARTING {
            @Override
            public void start(final Game g) {
                g.seconds = g.settings.countdownDuration;
            }

            @Override
            public void onSecond(final Game g) {
                if (g.players.size() < g.settings.minPlayers) {
                    g.setState(LOBBY);
                }
                g.sign.setStatusMessage("Starting in " + g.seconds);
                if (g.seconds == 0) {
                    g.setState(RUNNING);
                } else {
                    if (g.seconds == g.settings.teamSelectTime) {
                        g.selectTeams();
                    }
                    float percent = (float) g.seconds / (float) g.settings.countdownDuration;
                    for (GamePlayer p : g.players) {
                        BarAPI.setMessage(p.getHandle(), ChatColor.GREEN + "Starting in " + g.seconds, percent);
                    }
                    g.seconds--;
                }
            }
        },
        RUNNING {
            @Override
            public void start(Game g) {
                g.sign.setStatusMessage("Running");
                g.scoreboard.setScore("Bandits", 0);
                g.scoreboard.setScore("Rangers", 0);
                g.scoreboard.setScore("Bandit Leader", 0);
                for (GamePlayer p : g.players) {
                    BarAPI.setMessage(p.getHandle(), ChatColor.GREEN + "" + ChatColor.BOLD + "Rangers " + ChatColor.BLUE
                            + ChatColor.BOLD + "ALPHA" + ChatColor.GRAY + " | " + ChatColor.AQUA + "69.137.10.168", 1F);
                    p.setAlive(true);
                }
                for (GamePlayer p : g.teams.get(GameTeam.RANGERS)) {
                    p.getHandle().teleport(g.arena.getRangerSpawn());
                    Kit.RANGER.apply(p);
                }
                for (GamePlayer p : g.teams.get(GameTeam.BANDITS)) {
                    p.getHandle().teleport(g.arena.getBanditSpawn());
                    Kit.BANDIT.apply(p);
                }
            }

            @Override
            public void onSecond(Game g) {
                g.checkChest(GameTeam.RANGERS);
                g.checkChest(GameTeam.BANDITS);

                // Check victory conditions
                if (!g.banditLeader.isAlive()) {
                    g.setState(ENDING);
                    g.broadcast(ChatColor.RED + "The Bandit Leader has been defeated!");
                    g.broadcast(ChatColor.GREEN + "" + ChatColor.BOLD + "THE RANGERS WIN!");
                } else {
                    // If all the rangers are dead, the bandits have won
                    boolean rangerAlive = false;
                    for (GamePlayer ranger : g.teams.get(GameTeam.RANGERS))
                        if (ranger.isAlive())
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
                g.seconds = g.settings.restartDelay;
            }

            @Override
            public void onSecond(Game g) {
                if (g.seconds == 0) {
                    g.setState(LOBBY);
                } else {
                    for (GamePlayer p : g.players) {
                        BarAPI.setMessage(p.getHandle(), ChatColor.GREEN + "Restarting in " + g.seconds);
                    }
                    g.seconds--;
                }
            }
        };

        public abstract void start(final Game g);

        public abstract void onSecond(final Game g);
    }

    public Location getLobbySpawn() {
        return arena.getLobbySpawn();
    }

    public boolean allowPvp() {
        return state == State.RUNNING;
    }

    public CommonSettings getSettings() {
        return settings;
    }
}
