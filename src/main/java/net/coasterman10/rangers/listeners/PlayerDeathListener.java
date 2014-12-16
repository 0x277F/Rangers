package net.coasterman10.rangers.listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.game.GamePlayer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PlayerDeathListener implements Listener {
    private Map<UUID, Location> safeLocations = new HashMap<>();
    private Collection<Material> allowedDrops = new HashSet<>();

    public void setAllowedDrops(Collection<Material> allowedDrops) {
        this.allowedDrops = allowedDrops;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent e) {
        updateDropLocation(e.getPlayer(), e.getTo());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent e) {
        updateDropLocation(e.getPlayer(), e.getTo());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        GamePlayer player = PlayerManager.getPlayer(e.getPlayer());
        if (player.isAlive()) {
            dropHead(e.getPlayer());
            Location loc = safeLocations.get(player.id);
            if (loc != null) {
                for (ItemStack item : e.getPlayer().getInventory()) {
                    if (item == null)
                        continue;
                    Material type = item.getType();
                    if (type == Material.SKULL_ITEM || allowedDrops.contains(type)) {
                        Item i = loc.getWorld().dropItem(loc, item);
                        i.setVelocity(new Vector(0,0,0));
                        i.teleport(loc);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.getDrops().clear();
        if (PlayerManager.getPlayer(e.getEntity()).isAlive()) {
            dropHead(e.getEntity());
            Location loc = safeLocations.get(e.getEntity().getUniqueId());
            if (loc != null) {
                for (ItemStack item : e.getEntity().getInventory()) {
                    if (item == null)
                        continue;
                    Material type = item.getType();
                    if (type == Material.SKULL_ITEM || allowedDrops.contains(type)) {
                        Item i = loc.getWorld().dropItem(loc, item);
                        i.setVelocity(new Vector(0,0,0));
                        i.teleport(loc);
                    }
                }
            }
        }
    }

    private void updateDropLocation(Player player, Location loc) {
        if (!PlayerManager.getPlayer(player).isAlive()) {
            safeLocations.remove(player.getUniqueId());
            return;
        }

        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        for (int y = loc.getBlockY() + 1; y >= 0; y--) {
            Material type = loc.getWorld().getBlockAt(x, y, z).getType();
            if (type == Material.LAVA || type == Material.STATIONARY_LAVA || type == Material.FIRE)
                break;
            if (loc.getWorld().getBlockAt(x, y, z).getType().isSolid()) {
                loc.setX(x + 0.5);
                loc.setY(y + 1.25);
                loc.setZ(z + 0.5);
                safeLocations.put(player.getUniqueId(), loc);
                break;
            }
        }
    }

    public void dropHead(Player p) {
        GamePlayer player = PlayerManager.getPlayer(p);
        if (safeLocations.containsKey(player.id)) {
            player.dropHead(safeLocations.get(player.id));
        }
    }
}
