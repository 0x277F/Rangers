package net.coasterman10.rangers.arena;

import java.util.HashMap;
import java.util.Map;

import net.coasterman10.rangers.SpawnVector;
import net.coasterman10.rangers.config.ConfigUtil;
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
    private String name;
    private World world;
    private Location min, max;
    private Location lobby;
    private Location spectatorSpawn;
    private Map<GameTeam, Location> spawns = new HashMap<>();
    private Map<GameTeam, Location> chests = new HashMap<>();
    private boolean used;

    public Arena(String id) {
        this.id = id;
    }

    public void load(ConfigurationSection config) {
        name = config.getString("name");
        
        // Load the world. Necessary to make sense of any further values.
        String worldName = config.getString("world");
        if (worldName == null)
            return;
        world = Bukkit.getWorld(worldName);
        if (world == null)
            return;
        
        // Minimum and maximum positions for the arena.
        Vector minVector = ConfigUtil.getVector(config, "min");
        Vector maxVector = ConfigUtil.getVector(config, "max");
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
        SpawnVector lobbyVector = ConfigUtil.getSpawnVector(config, "lobby");
        SpawnVector spectatorSpawnVector = ConfigUtil.getSpawnVector(config, "spectator-spawn");
        if (lobbyVector != null)
            lobby = lobbyVector.toLocation(world);
        if (spectatorSpawnVector != null)
            spectatorSpawn = spectatorSpawnVector.toLocation(world);
        
        // Spawns and chests for each team.
        for (GameTeam team : GameTeam.values()) {
            SpawnVector spawnVector = ConfigUtil.getSpawnVector(config, "spawns." + team.name().toLowerCase());
            Vector chestVector = ConfigUtil.getVector(config, "chests." + team.name().toLowerCase());
            if (spawnVector != null)
                spawns.put(team, spawnVector.toLocation(world));
            if (chestVector != null)
                chests.put(team, chestVector.toLocation(world));
        }
    }

    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isUsed() {
        return used;
    }
    
    public Location getLobby() {
        return lobby;
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
