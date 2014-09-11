package net.coasterman10.rangers.map;

import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.game.GameTeam;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class Arena {
    private GameMap map;
    private Location origin;
    private boolean used;

    public Arena(GameMap map, Location origin) {
        this.map = map;
        this.origin = origin;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isUsed() {
        return used;
    }

    public String getMapName() {
        return map.name;
    }

    public void build(Plugin plugin) {
        map.getSchematic().buildDelayed(origin, plugin);
    }

    public Location getOrigin() {
        return origin;
    }

    public void sendToLobby(GamePlayer player) {
        player.getHandle().teleport(getLobbySpawn());
    }

    public void sendToGame(GamePlayer player) {
        if (player.getTeam() != null)
            player.getHandle().teleport(map.getSpawn(player.getTeam()).addTo(origin));
    }
    
    public Location getChest(GameTeam team) {
        return origin.clone().add(map.getChest(team));
    }

    public GameTeam getTeamOfChest(Location location) {
        Block b = location.getBlock();
        if (b.getType() == Material.CHEST || b.getType() == Material.ENDER_CHEST) {
            for (GameTeam team : GameTeam.values()) {
                if (team == GameTeam.SPECTATORS)
                    continue;
                if (map.getChest(team).equals(location))
                    return team;
            }
        }
        return null;
    }

    public void clearGround() {
        Vector min = origin.toVector();
        Vector max = min.clone().add(
                new Vector(map.getSchematic().getWidth(), origin.getWorld().getMaxHeight(), map.getSchematic()
                        .getLength()));
        for (Entity e : origin.getWorld().getEntitiesByClasses(Item.class, Arrow.class)) {
            if (e.getLocation().toVector().isInAABB(min, max)) {
                e.remove();
            }
        }
    }

    public Location getLobbySpawn() {
        return map.getLobbySpawn().addTo(origin);
    }

    public GameMap getMap() {
        return map;
    }
}
