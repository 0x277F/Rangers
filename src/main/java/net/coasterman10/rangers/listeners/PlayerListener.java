package net.coasterman10.rangers.listeners;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.coasterman10.rangers.GameSign;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerListener implements Listener {
    // These are initialized to failsafe values; they will and should be changed by the time any events fire.
    private Location lobby = Bukkit.getWorlds().get(0).getSpawnLocation();
    private Map<Location, GameSign> signs = new HashMap<>();
    private Collection<Material> allowedDrops = Collections.emptySet();
    
    public void setLobbyLocation(Location lobby) {
        this.lobby = lobby;
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
        e.getPlayer().teleport(lobby);
        e.getPlayer().getInventory().clear();
        e.getPlayer().getInventory().setArmorContents(null); // Essentials idiot devs still haven't figured this out
    }
    
    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null)
            return;
        Location loc = e.getClickedBlock().getLocation();
        if (signs.containsKey(loc)) {
            if (!signs.get(loc).getGame().addPlayer(e.getPlayer().getUniqueId())) {
                e.getPlayer().sendMessage(ChatColor.RED + "That game is full!");
            }
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
}
