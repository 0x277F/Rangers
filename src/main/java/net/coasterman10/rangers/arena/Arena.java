package net.coasterman10.rangers.arena;

import java.io.File;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

import net.coasterman10.rangers.GamePlayer;
import net.coasterman10.rangers.game.GameState;
import net.coasterman10.rangers.game.GameStateTasks;
import net.coasterman10.rangers.game.GameTeam;
import net.coasterman10.rangers.util.ConfigUtil;
import net.coasterman10.rangers.util.FileConfigAccessor;
import net.coasterman10.rangers.util.PlayerUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Arena implements Listener {
    private String name;
    private FileConfigAccessor config;
    private Location min, max;
    protected Location lobbySpawn;
    protected Location spectatorSpawn;
    protected Map<GameTeam, Location> spawns = new EnumMap<>(GameTeam.class);
    private Map<GameState, GameStateTasks> stateTasks = new EnumMap<>(GameState.class);
    private GameState state;
    protected Collection<GamePlayer> players = new HashSet<>();
    protected int seconds;

    public Arena(String name, FileConfigAccessor config, Plugin plugin) {
        this.name = name;
        this.config = config;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        new UpdateTask().runTaskTimer(plugin, 0L, 20L);
        state = GameState.LOBBY;
    }
    
    public void load() {
        config.reload();
        ConfigurationSection conf = getConfig();
        name = conf.getString("name", name);
        min = ConfigUtil.getLocation(conf, "bounds.min");
        max = ConfigUtil.getLocation(conf, "bounds.max");
        lobbySpawn = ConfigUtil.getLocation(conf, "lobby");
        spectatorSpawn = ConfigUtil.getLocation(conf, "spectator-spawn");
        for (GameTeam team : GameTeam.values()) {
            spawns.put(team, ConfigUtil.getLocation(conf, "spawns." + team.name().toLowerCase()));
        }
    }

    public void save() {
        ConfigurationSection conf = getConfig();
        conf.set("name", name);
        ConfigUtil.setLocation(conf, "bounds.min", min);
        ConfigUtil.setLocation(conf, "bounds.max", max);
        ConfigUtil.setLocation(conf, "lobby", lobbySpawn);
        ConfigUtil.setLocation(conf, "spectator-spawn", spectatorSpawn);
        for (GameTeam team : GameTeam.values()) {
            ConfigUtil.setLocation(conf, "spawns." + team.name().toLowerCase(), spawns.get(team));
        }
        config.save();
    }

    public void unload() {
        for (GamePlayer player : players) {
            removePlayer(player);
        }
        HandlerList.unregisterAll(this);
    }

    public final String getName() {
        return name;
    }

    public boolean rename(String newName) {
        save();
        File oldFile = config.getFile();
        File newFile = new File(oldFile.getParentFile(), newName + ".yml");
        if (!oldFile.renameTo(newFile)) {
            return false;
        }
        config = new FileConfigAccessor(newFile);
        config.reload();
        name = newName;
        getConfig().set("name", name);
        save();
        return true;
    }

    public void setMin(Location min) {
        this.min = min;
    }

    public void setMax(Location max) {
        this.max = max;
    }

    public void setLobbySpawn(Location lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    public void setSpectatorSpawn(Location spectatorSpawn) {
        this.spectatorSpawn = spectatorSpawn;
    }

    public void setSpawn(GameTeam team, Location spawn) {
        spawns.put(team, spawn);
    }

    public final int getPlayerCount() {
        return players.size();
    }

    public final int getMinPlayers() {
        return getConfig().getInt("min-players");
    }

    public final int getMaxPlayers() {
        return getConfig().getInt("max-players");
    }

    public GameState getState() {
        return state;
    }

    public int getSeconds() {
        return seconds;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public Location getSpectatorSpawn() {
        return spectatorSpawn;
    }

    public abstract ArenaType getType();

    public boolean isValid() {
        return name != null && min != null && max != null && lobbySpawn != null && spectatorSpawn != null;
    }

    public final boolean addPlayer(GamePlayer player) {
        if (players.contains(player)) {
            player.sendMessage(ChatColor.GOLD + "You are already in this game.");
            return false;
        } else if (players.size() >= getConfig().getInt("max-players")) {
            player.sendMessage(ChatColor.RED + "This game is full!");
            return false;
        } else {
            players.add(player);
            onPlayerJoin(player);
            Player handle = player.getHandle();
            handle.teleport(lobbySpawn);
            PlayerUtil.resetPlayer(handle);
            return true;
        }
    }

    public final boolean removePlayer(GamePlayer player) {
        if (!players.contains(player)) {
            player.sendMessage(ChatColor.GOLD + "You were not in this game.");
            return false;
        } else {
            players.remove(player);
            onPlayerLeave(player);
            return true;
        }
    }

    protected void registerStateTasks(GameState state, GameStateTasks tasks) {
        stateTasks.put(state, tasks);
    }

    protected void onPlayerJoin(GamePlayer player) {
        // Stub for subclasses to hook into
    }

    protected void onPlayerLeave(GamePlayer player) {
        // Stub for subclasses to hook into
    }

    protected final ConfigurationSection getConfig() {
        return config.get();
    }

    protected final void setState(GameState state) {
        if (this.state != state) {
            this.state = state;
            stateTasks.get(state).start();
        }
    }

    protected void clearEntities() {
        for (Entity e : min.getWorld().getEntities()) {
            if (e.getType() == EntityType.ARROW || e.getType() == EntityType.DROPPED_ITEM) {
                if (e.getLocation().toVector().isInAABB(min.toVector(), max.toVector())) {
                    e.remove();
                }
            }
        }
    }

    protected void broadcast(String message) {
        for (GamePlayer player : players) {
            player.sendMessage(message);
        }
    }

    private final class UpdateTask extends BukkitRunnable {
        @Override
        public void run() {
            GameStateTasks tasks = stateTasks.get(state);
            if (tasks != null)
                tasks.onSecond();
        }
    }
}
