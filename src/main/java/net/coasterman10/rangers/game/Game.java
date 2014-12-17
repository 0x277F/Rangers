package net.coasterman10.rangers.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.confuser.barapi.BarAPI;
import net.coasterman10.rangers.PlayerUtil;
import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.kits.Kit;
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
    private static final Random rand = new Random();
    private static int nextId;

    public final int id;
    private final GameSettings settings;
    private Arena arena;

    private GameScoreboard scoreboard;

    private Collection<GamePlayer> players = new HashSet<>();
    private Map<GameTeam, Collection<GamePlayer>> teams = new EnumMap<>(GameTeam.class);
    private Map<GameTeam, Collection<String>> headsToRedeem = new EnumMap<>(GameTeam.class);
    private GamePlayer banditLeader;

    private State state;
    private int seconds;

    public Game(GameSettings settings, Arena arena) {
        id = nextId++;

        this.settings = settings;
        this.arena = arena;
        arena.setGame(this);

        for (GameTeam team : GameTeam.values()) {
            teams.put(team, new HashSet<GamePlayer>());
            headsToRedeem.put(team, new HashSet<String>());
        }

        scoreboard = new GameScoreboard();

        state = State.LOBBY;

        new UpdateTask().runTaskTimer(Rangers.instance(), 0L, 20L);
    }

    public void addPlayer(GamePlayer player) {
        Player handle = player.getHandle();
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
        scoreboard.setForPlayer(handle);

        if (state == State.LOBBY && players.size() >= settings.minPlayers) {
            setState(State.STARTING);
        }
    }

    public void removePlayer(GamePlayer player) {
        if (!players.contains(player))
            return;
        broadcast(ChatColor.YELLOW + player.getHandle().getName() + ChatColor.AQUA + " left the game");
        player.setGame(null);
        player.setTeam(null);
        players.remove(player);
        for (Collection<GamePlayer> team : teams.values())
            team.remove(player);
        if (player.getHandle() != null) {
            player.getHandle().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            player.setCanDoubleJump(false);
            BarAPI.removeBar(player.getHandle());
            SpectateAPI.removeSpectator(player.getHandle());
            Rangers.instance().dropHead(player.getHandle());
        }

        if (state == State.STARTING && players.size() < settings.minPlayers)
            reset();
    }

    private void broadcast(String msg) {
        for (GamePlayer player : players)
            player.getHandle().sendMessage(msg);
    }

    private void reset() {
        for (Collection<GamePlayer> team : teams.values()) {
            team.clear();
        }
        for (GamePlayer p : players) {
            BarAPI.setMessage(p.getHandle(), settings.idleBarMessage, 100F);
            if (p.isAlive())
                arena.sendToLobby(p);
            PlayerUtil.resetPlayer(p.getHandle());
            p.setCanDoubleJump(false);
            p.setTeam(null);
            p.setAlive(false);
        }
        scoreboard.reset();
        banditLeader = null;
        setState(State.LOBBY);

        if (state == State.LOBBY && players.size() >= settings.minPlayers) {
            setState(State.STARTING);
        }
    }

    private void selectTeams() {
        GameTeam nextTeam = GameTeam.RANGERS;
        List<GamePlayer> playerList = new ArrayList<>(players);
        Collections.shuffle(playerList);
        for (GamePlayer p : playerList) {
            p.setTeam(nextTeam);
            scoreboard.setTeam(p.getHandle(), nextTeam);
            teams.get(nextTeam).add(p);
            p.getHandle().sendMessage(
                    ChatColor.AQUA + "You have been selected to join the " + ChatColor.YELLOW + nextTeam.name());
            nextTeam = nextTeam.opponent();
        }
    }

    public void start() {
        seconds = settings.timeLimit;
        scoreboard.setScore(GameTeam.RANGERS, 0);
        scoreboard.setScore(GameTeam.BANDITS, 0);

        arena.clearGround();

        List<GamePlayer> bandits = new ArrayList<>(teams.get(GameTeam.BANDITS));
        banditLeader = bandits.get(rand.nextInt(bandits.size()));
        scoreboard.setBanditLeader(banditLeader.getHandle());
        broadcast(ChatColor.YELLOW + banditLeader.getName() + ChatColor.AQUA + " is the " + ChatColor.RED
                + "Bandit Leader");

        for (GamePlayer p : players) {
            SpectateAPI.removeSpectator(p.getHandle());
            PlayerUtil.resetPlayer(p.getHandle());
            arena.sendToGame(p);
            p.setAlive(true);
            
            // UGLY HACK - If the map is named Moon, Jump Boost II, Weakness I, and Mining Fatigue I will be added
            if (arena.getName().equals("Moon")) {
                PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.JUMP, 1);
                PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.WEAKNESS, 0);
                PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.SLOW_DIGGING, 0);
            }
        }

        for (GamePlayer p : teams.get(GameTeam.RANGERS)) {
            Kit.RANGER.apply(p);
            p.setCanDoubleJump(true);
            PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.DAMAGE_RESISTANCE, 0);
            PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.SPEED, 0);
            headsToRedeem.get(GameTeam.RANGERS).add(p.getHandle().getName());
        }

        // If rangers and bandits are unbalanced, do not give bandits slowness
        boolean slowness = teams.get(GameTeam.RANGERS).size() == teams.get(GameTeam.BANDITS).size()
                && teams.get(GameTeam.BANDITS).size() < 2;
        for (GamePlayer p : teams.get(GameTeam.BANDITS)) {
            Kit.BANDIT.apply(p);
            PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.DAMAGE_RESISTANCE, 0);
            if (slowness)
                PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.SLOW, 0);
        }

        headsToRedeem.get(GameTeam.BANDITS).add(banditLeader.getName());
    }

    private void checkChest(GameTeam t) {
        Location loc = arena.getChest(t);
        if (loc.getBlock().getState() instanceof Chest) {
            Chest state = (Chest) loc.getBlock().getState();
            for (ItemStack item : state.getBlockInventory()) {
                if (item != null && item.getType() == Material.SKULL_ITEM) {
                    SkullMeta meta = (SkullMeta) item.getItemMeta();
                    if (meta.hasOwner()) {
                        if (headsToRedeem.get(t.opponent()).remove(meta.getOwner())) {
                            scoreboard.incrementScore(t.opponent());
                        }
                        for (GamePlayer player : teams.get(t)) {
                            if (meta.getOwner().equals(player.getHandle().getName())) {
                                loc.getWorld().dropItem(loc.getBlock().getRelative(BlockFace.UP).getLocation(), item);
                            }
                        }
                    }
                }
            }
            state.getBlockInventory().clear();
        }
    }

    private void onSecond() {
        if (seconds == 0) {
            broadcast(ChatColor.RED + "Time has expired!");
            broadcast(ChatColor.GOLD + "" + ChatColor.BOLD + "The game is a draw.");
            setState(State.ENDING);
        } else {
            for (GamePlayer player : players) {
                int m = seconds / 60; // Minutes
                int s = seconds % 60; // Seconds
                float percent = (float) seconds / (float) settings.timeLimit * 100F;
                BarAPI.setMessage(player.getHandle(), (seconds < 30 ? ChatColor.RED : ChatColor.GREEN).toString() + m
                        + ":" + (s < 10 ? "0" + s : s) + " remaining", percent);
            }

            checkChest(GameTeam.RANGERS);
            checkChest(GameTeam.BANDITS);

            // Check victory conditions
            if (headsToRedeem.get(GameTeam.RANGERS).isEmpty()) {
                setState(State.ENDING);
                broadcast(ChatColor.RED + "The rangers have been defeated!");
                broadcast(ChatColor.GREEN + "" + ChatColor.BOLD + "THE BANDITS WIN!");
            }

            if (headsToRedeem.get(GameTeam.BANDITS).isEmpty()) {
                setState(State.ENDING);
                broadcast(ChatColor.RED + "The bandit leader has been killed!");
                broadcast(ChatColor.GREEN + "" + ChatColor.BOLD + "THE RANGERS WIN!");
            }

            seconds--;
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
        LOBBY {
            @Override
            public void start(final Game g) {

            }

            @Override
            public void onSecond(final Game g) {

            }
        },
        STARTING {
            @Override
            public void start(final Game g) {
                g.seconds = g.settings.countdownDuration;
            }

            @Override
            public void onSecond(final Game g) {
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
                g.start();
            }

            @Override
            public void onSecond(Game g) {
                g.onSecond();
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
                    g.reset();
                } else {
                    float percent = (float) g.seconds / (float) g.settings.restartDelay * 100F;
                    for (GamePlayer p : g.players) {
                        BarAPI.setMessage(p.getHandle(), ChatColor.GREEN + "Restarting in " + g.seconds, percent);
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

    public Arena getArena() {
        return arena;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public GamePlayer getBanditLeader() {
        return banditLeader;
    }

    public State getState() {
        return state;
    }

    public int getSeconds() {
        return seconds;
    }

    public GamePlayer getRandomAlivePlayer(GameTeam team) {
        List<GamePlayer> alivePlayers = new ArrayList<>(teams.get(team));
        for (Iterator<GamePlayer> it = alivePlayers.iterator(); it.hasNext();)
            if (it.next().isAlive())
                it.remove();
        return alivePlayers.get(rand.nextInt(alivePlayers.size()));
    }
}
