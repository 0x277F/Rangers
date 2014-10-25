package net.coasterman10.rangers.arena;

import java.util.HashMap;
import java.util.Map;

import net.coasterman10.rangers.SpawnVector;
import net.coasterman10.rangers.config.ConfigSectionAccessor;
import net.coasterman10.rangers.config.ConfigUtil;
import net.coasterman10.rangers.game.Game;
import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.game.GameTeam;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;

public class Arena {
    private final String id;
    private final ConfigSectionAccessor config;
    private String name;
    private World world;
    private Location min, max;
    private Location lobby;
    private Location spectatorSpawn;
    private Map<GameTeam, Location> spawns = new HashMap<>();
    private Map<GameTeam, Location> chests = new HashMap<>();
    private Game game;
    private boolean active;
    
    public Arena(String id, ConfigSectionAccessor config) {
        this.id = id;
        this.config = config;
    }

    public void load() {
        config.reload();
        ConfigurationSection conf = config.get();

        name = conf.getString("name");

        // Load the world. Necessary to make sense of any further values.
        String worldName = conf.getString("world");
        if (worldName == null)
            return;
        world = Bukkit.getWorld(worldName);
        if (world == null)
            return;

        // Minimum and maximum positions for the arena.
        Vector minVector = ConfigUtil.getVector(conf, "min");
        Vector maxVector = ConfigUtil.getVector(conf, "max");
        if (minVector != null)
            min = minVector.toLocation(world);
        if (maxVector != null)
            max = maxVector.toLocation(world);

        // Correct the coordinates if necessary.
        // TODO Offload to cleaner utility method
        if (min.getX() > max.getX()) {
            double x = min.getX();
            min.setX(max.getX());
            max.setX(x);
        }
        if (min.getY() > max.getY()) {
            double y = min.getY();
            min.setY(max.getY());
            max.setY(y);
        }
        if (min.getZ() > max.getZ()) {
            double z = min.getZ();
            min.setZ(max.getZ());
            max.setZ(z);
        }

        // Lobby and spectator spawns.
        SpawnVector lobbyVector = ConfigUtil.getSpawnVector(conf, "lobby");
        SpawnVector spectatorSpawnVector = ConfigUtil.getSpawnVector(conf, "spectator-spawn");
        if (lobbyVector != null)
            lobby = lobbyVector.toLocation(world);
        if (spectatorSpawnVector != null)
            spectatorSpawn = spectatorSpawnVector.toLocation(world);

        // Spawns and chests for each team.
        for (GameTeam team : GameTeam.values()) {
            SpawnVector spawnVector = ConfigUtil.getSpawnVector(conf, "spawns." + team.name().toLowerCase());
            Vector chestVector = ConfigUtil.getVector(conf, "chests." + team.name().toLowerCase());
            if (spawnVector != null)
                spawns.put(team, spawnVector.toLocation(world));
            if (chestVector != null)
                chests.put(team, chestVector.toLocation(world));
        }
    }

    public void save() {
        ConfigurationSection conf = config.get();

        conf.set("name", name);
        conf.set("world", world != null ? world.getName() : null);

        ConfigUtil.setVector(conf, "min", min != null ? min.toVector() : null);
        ConfigUtil.setVector(conf, "max", max != null ? max.toVector() : null);

        ConfigUtil.setVector(conf, "lobby", lobby != null ? new SpawnVector(lobby) : null);
        ConfigUtil.setVector(conf, "spectator-spawn", spectatorSpawn != null ? new SpawnVector(spectatorSpawn) : null);

        for (GameTeam team : GameTeam.values()) {
            SpawnVector spawn = spawns.get(team) != null ? new SpawnVector(spawns.get(team)) : null;
            Vector chest = chests.get(team) != null ? chests.get(team).toVector() : null;
            ConfigUtil.setVector(conf, "spawns." + team.name().toLowerCase(), spawn);
            ConfigUtil.setVector(conf, "chests." + team.name().toLowerCase(), chest);
        }

        config.save();
    }
    
    public Game getGame() {
        return game;
    }
    
    public void setGame(Game game) {
        this.game = game;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public boolean hasGame() {
        return game != null;
    }

    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }

    public Location getLobby() {
        return lobby;
    }

    public void setMin(Location min) {
        this.min = min;
        this.world = min.getWorld(); // TODO Check that all locations are in this world
    }

    public void setMax(Location max) {
        this.max = max;
    }

    public void setSpawn(GameTeam team, Location spawn) {
        spawns.put(team, spawn);
    }

    public void setChest(GameTeam team, Location chest) {
        chests.put(team, chest);
    }

    public void setLobby(Location lobby) {
        this.lobby = lobby;
    }

    public void setSpectatorSpawn(Location spectatorSpawn) {
        this.spectatorSpawn = spectatorSpawn;
    }

    public void sendToLobby(GamePlayer player) {
        player.getHandle().teleport(lobby);
    }

    public void sendToGame(GamePlayer player) {
        if (player.getTeam() != null)
            player.getHandle().teleport(spawns.get(player.getTeam()));
    }

    public void sendSpectatorToGame(GamePlayer player) {
        player.getHandle().teleport(spectatorSpawn);
    }

    public Location getChest(GameTeam team) {
        return chests.get(team);
    }

    public GameTeam getTeamOfChest(Location location) {
        Block b = location.getBlock();
        if (b.getType() == Material.CHEST || b.getType() == Material.ENDER_CHEST) {
            for (GameTeam team : GameTeam.values()) {
                if (getChest(team).equals(location))
                    return team;
            }
        }
        return null;
    }

    public void clearGround() {
        for (Entity e : world.getEntitiesByClasses(Item.class, Arrow.class)) {
            if (e.getLocation().toVector().isInAABB(min.toVector(), max.toVector())) {
                e.remove();
            }
        }
    }

    public boolean isValid() {
        if (name == null || world == null || min == null || max == null || lobby == null || spectatorSpawn == null)
            return false;
        for (GameTeam team : GameTeam.values())
            if (spawns.get(team) == null || chests.get(team) == null)
                return false;
        return true;
    }
}
