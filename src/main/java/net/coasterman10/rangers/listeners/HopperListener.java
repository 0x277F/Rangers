package net.coasterman10.rangers.listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import net.coasterman10.rangers.Game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class HopperListener implements Listener {
    private Collection<Game> games = new HashSet<>();
    private Map<ItemStack, UUID> lastOwners = new HashMap<>();

    public void setGames(Collection<Game> games) {
        this.games = games;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (e.getItemDrop().getItemStack().getType() == Material.SKULL)
            lastOwners.put(e.getItemDrop().getItemStack(), e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent e) {
        if (e.getItem().getItemStack().getType() == Material.SKULL
                && e.getInventory().getHolder() instanceof BlockState) {
            SkullMeta meta = (SkullMeta) e.getItem().getItemStack().getItemMeta();
            if (meta.hasOwner()) {
                for (Game g : games) {
                    if (g.hasHopper(((BlockState) e.getInventory().getHolder()).getLocation())) {
                        if (!g.checkHopper(((BlockState) e.getInventory().getHolder()).getLocation(), e.getItem()
                                .getItemStack())) {
                            e.setCancelled(true);
                            if (lastOwners.containsKey(e.getItem().getItemStack())) {
                                if (Bukkit.getPlayer(lastOwners.get(e.getItem().getItemStack())) != null) {
                                    Bukkit.getPlayer(lastOwners.get(e.getItem().getItemStack())).getInventory()
                                            .addItem(e.getItem().getItemStack());
                                    Bukkit.getPlayer(lastOwners.get(e.getItem())).sendMessage(
                                            ChatColor.RED + "You placed this head in the wrong hopper!");
                                    e.getItem().remove();
                                }
                                lastOwners.remove(e.getItem().getItemStack());
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent e) {
        if (e.getItem().getType() == Material.SKULL && e.getDestination().getHolder() instanceof BlockState) {
            SkullMeta meta = (SkullMeta) e.getItem().getItemMeta();
            if (meta.hasOwner()) {
                for (Game g : games) {
                    if (g.hasHopper(((BlockState) e.getDestination().getHolder()).getLocation())) {
                        g.checkHopper(((BlockState) e.getDestination().getHolder()).getLocation(), e.getItem());
                    }
                }
            }
        }
    }
}
