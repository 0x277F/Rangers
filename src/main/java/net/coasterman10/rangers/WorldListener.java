package net.coasterman10.rangers;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;

public class WorldListener implements Listener {
    @EventHandler
    public void onItemDespawn(ItemDespawnEvent e) {
        if (e.getEntity().getItemStack().getType() == Material.SKULL)
            e.setCancelled(true);
    }
}
