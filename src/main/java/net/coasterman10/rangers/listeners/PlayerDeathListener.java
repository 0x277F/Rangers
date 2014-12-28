package net.coasterman10.rangers.listeners;

import java.util.Collection;
import java.util.HashSet;

import net.coasterman10.rangers.GamePlayer;
import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.arena.ClassicArena;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDeathListener implements Listener {
    private Collection<Material> allowedDrops = new HashSet<>();

    public void setAllowedDrops(Collection<Material> allowedDrops) {
        this.allowedDrops = allowedDrops;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        GamePlayer player = PlayerManager.getPlayer(e.getPlayer());
        if (player.isAlive() && player.getArena() instanceof ClassicArena) {
            player.dropHead();
            player.dropInventory(allowedDrops);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.getDrops().clear();
        GamePlayer player = PlayerManager.getPlayer(e.getEntity());
        if (player.isAlive() && player.getArena() instanceof ClassicArena) {
            player.setAlive(false);
            player.dropHead();
            player.dropInventory(allowedDrops);
        }
    }
}
