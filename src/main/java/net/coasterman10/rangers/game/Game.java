package net.coasterman10.rangers.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import me.confuser.barapi.BarAPI;
import net.coasterman10.rangers.PlayerUtil;
import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.kits.Kit;
import net.coasterman10.rangers.map.Arena;
import net.coasterman10.spectate.SpectateAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Game {
    private static int nextId;

    private final int id;
    private final GameSettings settings;
    private Arena arena;

    private GameScoreboard scoreboard;

    private Collection<GamePlayer> players = new HashSet<>();
    private Map<GameTeam, Collection<GamePlayer>> teams = new EnumMap<>(GameTeam.class);
    private GamePlayer banditLeader;

    // Initial ranger head-count unaffected by rangers leaving the game
    private int totalRangers;

    private State state;
    private int seconds;

    public Game(Rangers plugin, GameSettings settings) {
        id = nextId++;

        this.settings = settings;

        teams.put(GameTeam.RANGERS, new HashSet<GamePlayer>());
        teams.put(GameTeam.BANDITS, new HashSet<GamePlayer>());

        scoreboard = new GameScoreboard();

        state = State.INACTIVE;

        new UpdateTask().runTaskTimer(plugin, 0L, 20L);
    }

    public void setArena(Arena arena) {
        this.arena = arena;
        arena.setUsed(true);
        state = State.LOBBY;
    }

    public int getId() {
        return id;
    }

    public void addPlayer(GamePlayer player) {
        Player handle = player.getHandle();

        if (state == State.INACTIVE) {
            handle.sendMessage(ChatColor.RED + "That game is not set up correctly, please notify an administrator.");
            return;
        }

        if (state == State.LOBBY || (state == State.STARTING && seconds > settings.teamSelectTime)) {
            if (players.size() == settings.maxPlayers) {
                handle.sendMessage(ChatColor.RED + "This game is full!");
                return;
            }
            players.add(player);
            player.setGame(this);
            player.setTeam(null);
            PlayerUtil.resetPlayer(handle);
            arena.sendToLobby(player);
            broadcast(ChatColor.YELLOW + handle.getName() + ChatColor.AQUA + " joined the game");
        } else {
            handle.sendMessage(ChatColor.DARK_AQUA
                    + "The game is already in progress. You can spectate until it restarts.");
            players.add(player);
            player.setGame(this);
            player.setTeam(GameTeam.SPECTATORS);
            PlayerUtil.resetPlayer(handle);
            SpectateAPI.addSpectator(handle);
            
        }
    }

    public void removePlayer(GamePlayer player) {
        if (!players.contains(player))
            return;
        players.remove(player);
        player.setGame(null);
        player.setTeam(null);
        for (Collection<GamePlayer> team : teams.values())
            team.remove(player);
        if (player.getHandle() != null) {
            BarAPI.removeBar(player.getHandle());
            player.getHandle().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            PlayerUtil.disableDoubleJump(player.getHandle());
            SpectateAPI.removeSpectator(player.getHandle());
        }
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
                p.setBanditLeader(true);
            } else {
                p.setBanditLeader(false);
            }
            team = team.opponent();
        }

        for (GameTeam t : GameTeam.values()) {
            for (GamePlayer p : teams.get(t)) {
                scoreboard.setTeam(p.getHandle(), team);
                p.getHandle().setScoreboard(scoreboard.getScoreboard(t));
            }
        }

        totalRangers = teams.get(GameTeam.RANGERS).size();

        scoreboard.setBanditLeader(banditLeader.getHandle());
    }

    private void checkChest(GameTeam t) {
        Location loc = arena.getChest(t);
        if (loc.getBlock().getState() instanceof Chest) {
            Chest state = (Chest) loc.getBlock().getState();
            next: for (ItemStack item : state.getBlockInventory()) {
                if (item != null) {
                    state.getBlockInventory().remove(item);
                    if (item.getType() == Material.SKULL_ITEM) {
                        SkullMeta meta = (SkullMeta) item.getItemMeta();
                        if (meta.hasOwner()) {
                            // Rangers win if this is bandit chest and bandit leader's head is in it
                            if (meta.getOwner().equals(banditLeader.getHandle().getName())) {
                                setState(State.ENDING);
                                broadcast(ChatColor.RED + "The bandit leader has been killed!");
                                broadcast(ChatColor.GREEN + "" + ChatColor.BOLD + "THE RANGERS WIN!");
                            }
                            
                            // Score points for enemy heads placed in the chest
                            for (GamePlayer p : teams.get(t.opponent())) {
                                if (meta.getOwner().equals(p.getHandle().getName())) {
                                    scoreboard.incrementScore(t.opponent());
                                    break next; // I know this sucks
                                }
                            }

                            // Remove heads from the chest's own team that don't belong in it
                            for (GamePlayer p : teams.get(t)) {
                                if (meta.getOwner().equals(p.getHandle().getName())) {
                                    loc.getWorld().dropItemNaturally(
                                            loc.getBlock().getRelative(BlockFace.UP).getLocation(), item);
                                }
                            }
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

    public static enum State {
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
                for (Collection<GamePlayer> team : g.teams.values()) {
                    team.clear();
                }
                for (GamePlayer p : g.players) {
                    BarAPI.removeBar(p.getHandle());
                    g.arena.sendToLobby(p);
                    p.setTeam(null);
                    PlayerUtil.resetPlayer(p.getHandle());
                    PlayerUtil.disableDoubleJump(p.getHandle());
                }
                g.scoreboard.reset();
            }

            @Override
            public void onSecond(final Game g) {
                for (GamePlayer p : g.players) {
                    BarAPI.setMessage(p.getHandle(), ChatColor.GREEN + "" + ChatColor.BOLD + "Rangers "
                            + ChatColor.BLUE + ChatColor.BOLD + "ALPHA" + ChatColor.GRAY + " | " + ChatColor.AQUA
                            + "70.114.250.251", 100F);
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
                if (g.seconds == 0) {
                    g.setState(RUNNING);
                } else {
                    if (g.seconds == g.settings.teamSelectTime) {
                        g.selectTeams();
                    }
                    float percent = (float) g.seconds / (float) g.settings.countdownDuration * 100F;
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
                g.seconds = g.settings.timeLimit;
                g.scoreboard.setScore(GameTeam.RANGERS, 0);
                g.scoreboard.setScore(GameTeam.BANDITS, 0);
                for (GamePlayer p : g.players) {
                    PlayerUtil.resetPlayer(p.getHandle());
                    g.arena.sendToGame(p);
                }
                for (GamePlayer p : g.teams.get(GameTeam.RANGERS)) {
                    Kit.RANGER.apply(p);
                    PlayerUtil.enableDoubleJump(p.getHandle());
                    PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.DAMAGE_RESISTANCE, 0);
                    PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.SPEED, 0);
                }
                for (GamePlayer p : g.teams.get(GameTeam.BANDITS)) {
                    Kit.BANDIT.apply(p);
                    PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.DAMAGE_RESISTANCE, 0);
                    PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.SLOW, 0);
                }

                g.arena.clearGround();
            }

            @Override
            public void onSecond(Game g) {
                if (g.seconds == 0) {
                    g.setState(ENDING);
                    g.broadcast(ChatColor.RED + "Time has expired!");
                    g.broadcast(ChatColor.GOLD + "" + ChatColor.BOLD + "The game is a draw.");
                } else {
                    for (GamePlayer player : g.players) {
                        int minutes = g.seconds / 60;
                        int seconds = g.seconds % 60;
                        float percent = (float) g.seconds / (float) g.settings.timeLimit * 100F;
                        BarAPI.setMessage(player.getHandle(),
                                (g.seconds < 30 ? ChatColor.RED : ChatColor.GREEN).toString() + minutes + ":"
                                        + (seconds < 10 ? "0" + seconds : seconds) + " remaining", percent);
                    }

                    g.checkChest(GameTeam.RANGERS);
                    g.checkChest(GameTeam.BANDITS);

                    // Check ranger victory conditions
                    if (g.scoreboard.getScore(GameTeam.RANGERS) == g.totalRangers) {
                        g.setState(ENDING);
                        g.broadcast(ChatColor.RED + "The rangers have been defeated!");
                        g.broadcast(ChatColor.GREEN + "" + ChatColor.BOLD + "THE BANDITS WIN!");
                    }
                    g.seconds--;
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

    public boolean allowPvp() {
        return state == State.RUNNING;
    }

    public GameSettings getSettings() {
        return settings;
    }

    public boolean isRunning() {
        return state == State.RUNNING;
    }

    public String getMapName() {
        return arena.getMapName();
    }

    public Arena getArena() {
        return arena;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public State getState() {
        return state;
    }

    public int getSeconds() {
        return seconds;
    }
}