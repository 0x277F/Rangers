package net.coasterman10.rangers;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerListener implements Listener {
    private Location lobby;
    private Collection<Material> allowedDrops = Collections.emptySet();
    
    public void setLobbyLocation(Location lobby) {
        this.lobby = lobby;
    }
    
    public void setAllowedDrops(Collection<Material> allowedDrops) {
        this.allowedDrops = allowedDrops;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.getPlayer().sendMessage("Welcome to Rangers!");
        e.getPlayer().teleport(lobby);
        e.getPlayer().getInventory().clear();
        e.getPlayer().getInventory().setArmorContents(null); // Essentials idiot devs still haven't figured this out
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
