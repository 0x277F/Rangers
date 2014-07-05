package net.coasterman10.rangers.listeners;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.coasterman10.rangers.GameSign;
import net.coasterman10.rangers.PlayerData;
import net.coasterman10.rangers.Rangers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerListener implements Listener {
    private final Rangers plugin;

    // These are initialized to failsafe values; they will and should be changed by the time any events fire.
    private Map<Location, GameSign> signs = new HashMap<>();
    private Collection<Material> allowedDrops = Collections.emptySet();

    public PlayerListener(Rangers plugin) {
        this.plugin = plugin;
    }

    public void setAllowedDrops(Collection<Material> allowedDrops) {
        this.allowedDrops = allowedDrops;
    }

    public void setSigns(Map<Location, GameSign> signs) {
        this.signs = signs;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().sendMessage("Welcome to Rangers!");
        e.getPlayer().teleport(plugin.getLobbySpawn());
        e.getPlayer().getInventory().clear();
        e.getPlayer().getInventory().setArmorContents(null); // Essentials idiot devs still haven't figured this out
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        PlayerData data = plugin.getPlayerData(e.getPlayer());
        if (data.getGame() != null)
            data.getGame().removePlayer(e.getPlayer());
        plugin.removePlayerData(e.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        Location loc = e.getClickedBlock().getLocation();
        if (signs.containsKey(loc)) {
            signs.get(loc).getGame().addPlayer(e.getPlayer());
        }
        if (e.getClickedBlock().getType() == Material.CHEST && e.getPlayer().getItemInHand() != null
                && e.getPlayer().getItemInHand().getType() == Material.SKULL_ITEM) {
            ((Chest) e.getClickedBlock().getState()).getBlockInventory().addItem(e.getPlayer().getItemInHand());
            e.getPlayer().getInventory().remove(e.getPlayer().getItemInHand());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        // Only drop allowed items (food, possibly other items in future)
        for (Iterator<ItemStack> it = e.getDrops().iterator(); it.hasNext();)
            if (!allowedDrops.contains(it.next().getType()))
                it.remove();

        // Drop the victim's head
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(e.getEntity().getName());
        head.setItemMeta(meta);
        e.getDrops().add(head);
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent e) {
        if (e.getItem().getItemStack().getType() == Material.SKULL_ITEM) {
            SkullMeta meta = (SkullMeta) e.getItem().getItemStack().getItemMeta();
            if (meta.hasOwner()) {
                @SuppressWarnings("deprecation")
                Player owner = Bukkit.getPlayer(meta.getOwner());
                PlayerData ownerData = plugin.getPlayerData(owner);
                PlayerData pickupData = plugin.getPlayerData(e.getPlayer());
                if (ownerData.getTeam() == pickupData.getTeam()) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
