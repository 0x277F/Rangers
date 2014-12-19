package net.coasterman10.rangers.game;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

import me.confuser.barapi.BarAPI;
import net.coasterman10.rangers.PlayerUtil;
import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.spectate.SpectateAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Game implements Listener {
    private static int nextId;

    public final int id;
    protected final GameSettings settings;
    protected final Arena arena;
    protected Collection<GamePlayer> players = new HashSet<>();
    protected State state;
    protected Map<State, Runnable> stateTasks = new EnumMap<>(State.class);
    protected int seconds;

    public Game(GameSettings settings, Arena arena, Plugin plugin) {
        id = nextId++;

        this.settings = settings;
        this.arena = arena;
        arena.setGame(this);

        state = State.LOBBY;
        
        new BukkitRunnable() {
            private State currentState;
            private Runnable currentTask;
            
            @Override
            public void run() {
                if (state != currentState) {
                    currentState = state;
                    currentTask = stateTasks.get(currentState);
                }
                if (currentTask != null) {
                    currentTask.run();
                    if (state != currentState) {
                        run();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    public void register(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    public void unregister(Plugin plugin) {
        HandlerList.unregisterAll(this);
    }

    public void addPlayer(GamePlayer player) {
        if (players.contains(player))
            return;
        if (players.size() == settings.maxPlayers) {
            player.getHandle().sendMessage(ChatColor.RED + "This game is full!");
            return;
        }

        Player handle = player.getHandle();
        players.add(player);
        player.setGame(this);
        player.setTeam(null);
        PlayerUtil.resetPlayer(handle);
        arena.sendToLobby(player);
        broadcast(ChatColor.WHITE + handle.getName() + ChatColor.DARK_AQUA + " joined the game");

        if (state == State.LOBBY && players.size() >= settings.minPlayers) {
            state = State.STARTING;
            seconds = settings.countdownDuration;
        }
    }

    public void removePlayer(GamePlayer player) {
        if (!players.contains(player))
            return;
        broadcast(ChatColor.WHITE + player.getHandle().getName() + ChatColor.DARK_AQUA + " left the game");
        player.setGame(null);
        player.setTeam(null);
        players.remove(player);

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

    public enum State {
        LOBBY, STARTING, RUNNING, ENDING;
    }

    public boolean allowPvp() {
        return state == State.RUNNING;
    }

    public GameSettings getSettings() {
        return settings;
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

    protected void broadcast(String msg) {
        for (GamePlayer player : players)
            player.getHandle().sendMessage(msg);
    }

    protected abstract void reset();

    protected abstract void selectTeams();

    protected abstract void start();

    protected abstract void onSecond();
}
