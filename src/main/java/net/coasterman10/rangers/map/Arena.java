package net.coasterman10.rangers.map;

import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.game.GameTeam;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
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
        new BukkitRunnable() {
            final World w = origin.getWorld();
            final int x0 = origin.getBlockX();
            final int y0 = origin.getBlockY();
            final int z0 = origin.getBlockZ();
            final Vector size = map.getSchematic().getSize();
            int pos;

            @Override
            public void run() {
                if (size.getBlockX() == 0 || size.getBlockY() == 0 || size.getBlockZ() == 0) {
                    cancel();
                    return;
                }

                for (int i = 0; i < size.getBlockX(); i++) {
                    for (int j = 0; j < size.getBlockY(); j++) {
                        int k = pos;
                        map.getSchematic().getBlock(i, j, k).build(w.getBlockAt(x0 + i, y0 + j, z0 + k));
                    }
                }
                pos++;
                if (pos == size.getBlockZ())
                    cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
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

    public void sendSpectatorToGame(GamePlayer player) {
        player.getHandle().teleport(map.getSpectatorSpawn().addTo(origin));
    }

    public Location getChest(GameTeam team) {
        return origin.clone().add(map.getChest(team));
    }

    public GameTeam getTeamOfChest(Location location) {
        Block b = location.getBlock();
        if (b.getType() == Material.CHEST || b.getType() == Material.ENDER_CHEST) {
            for (GameTeam team : GameTeam.values()) {
                if (map.getChest(team).equals(location))
                    return team;
            }
        }
        return null;
    }

    public void clearGround() {
        Vector min = origin.toVector();
        Vector max = min.clone().add(map.getSchematic().getSize().setY(origin.getWorld().getMaxHeight()));
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
