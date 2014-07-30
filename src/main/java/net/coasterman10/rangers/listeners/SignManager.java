package net.coasterman10.rangers.listeners;

import java.util.HashMap;
import java.util.Map;

import net.coasterman10.rangers.Game;
import net.coasterman10.rangers.GameSign;
import net.coasterman10.rangers.PlayerManager;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignManager implements Listener {
    private Map<Location, GameSign> signs = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (signs.containsKey(e.getClickedBlock().getLocation())) {
                Game g = signs.get(e.getClickedBlock().getLocation()).getGame();
                if (g != null) {
                    g.addPlayer(PlayerManager.getPlayer(e.getPlayer()));
                }
            }
        }
    }
}
