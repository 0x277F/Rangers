package net.coasterman10.rangers.listeners;

import java.util.Collection;
import java.util.HashSet;

import net.coasterman10.rangers.arena.ClassicArena;
import net.coasterman10.rangers.player.RangersPlayer;
import net.coasterman10.rangers.player.RangersPlayer.PlayerState;

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
        RangersPlayer player = RangersPlayer.getPlayer(e.getPlayer());
        if (player.isPlaying() && player.getArena() instanceof ClassicArena) {
            player.dropHead();
            player.dropInventory(allowedDrops);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.getDrops().clear();
        RangersPlayer player = RangersPlayer.getPlayer(e.getEntity());
        if (player.isPlaying() && player.getArena() instanceof ClassicArena) {
            player.setState(PlayerState.GAME_LOBBY);
            player.dropHead();
            player.dropInventory(allowedDrops);
        }
    }
}
