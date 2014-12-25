package net.coasterman10.rangers.listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.arena.ClassicArena;
import net.coasterman10.rangers.game.GamePlayer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
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
        PlayerManager.getPlayer(e.getPlayer()).updateSafeLocation();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent e) {
        PlayerManager.getPlayer(e.getPlayer()).updateSafeLocation();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        GamePlayer player = PlayerManager.getPlayer(e.getPlayer());
        if (player.isAlive() && player.getArena() instanceof ClassicArena) {
            player.dropHead();
            Location loc = safeLocations.get(player.id);
            if (loc != null) {
                for (ItemStack item : e.getPlayer().getInventory()) {
                    if (item == null)
                        continue;
                    Material type = item.getType();
                    if (type == Material.SKULL_ITEM || allowedDrops.contains(type)) {
                        Item i = loc.getWorld().dropItem(loc, item);
                        i.setVelocity(new Vector(0, 0, 0));
                        i.teleport(loc);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.getDrops().clear();
        GamePlayer player = PlayerManager.getPlayer(e.getEntity());
        if (player.isAlive() && player.getArena() instanceof ClassicArena) {
            player.dropHead();
            Location loc = safeLocations.get(e.getEntity().getUniqueId());
            if (loc != null) {
                for (ItemStack item : e.getEntity().getInventory()) {
                    if (item == null)
                        continue;
                    Material type = item.getType();
                    if (type == Material.SKULL_ITEM || allowedDrops.contains(type)) {
                        Item i = loc.getWorld().dropItem(loc, item);
                        i.setVelocity(new Vector(0, 0, 0));
                        i.teleport(loc);
                    }
                }
            }
        }
    }
}
