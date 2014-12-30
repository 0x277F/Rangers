package net.coasterman10.rangers.stats;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;

public class StatListener implements Listener{

    @EventHandler
    public void onPlayerConnect(PlayerJoinEvent e){
        try {
            StatManager.loadFromFile(e.getPlayer().getUniqueId());
        } catch (InvalidConfigurationException | IOException x){
            x.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        StatManager.unloadStat(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if(e.getClickedInventory().getName().endsWith("Statistics")){
            e.setCancelled(true);
        }
    }
}
