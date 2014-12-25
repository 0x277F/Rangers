package net.coasterman10.rangers.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.coasterman10.rangers.menu.PreferenceMenu;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.blablubbabc.insigns.SignSendEvent;

public class MenuManager implements Listener {
    private Map<Location, PreferenceMenu> signMenus = new HashMap<>();
    private Map<UUID, PreferenceMenu> currentMenus = new HashMap<>();

    public void addSignMenu(Location loc, PreferenceMenu menu) {
        signMenus.put(loc, menu);
    }
    
    public void addSignMenus(Map<Location, PreferenceMenu> menus) {
        signMenus.putAll(menus);
    }

    @EventHandler
    public void onSignSend(SignSendEvent e) {
        PreferenceMenu menu = signMenus.get(e.getLocation());
        if (menu != null) {
            e.setLine(0, "Ranger/Bandit");
            e.setLine(1, "Preferences");
            e.setLine(2, ChatColor.GREEN + "[Selection]");
            e.setLine(3, "Click To Change");
        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null) {
            if (e.getClickedBlock().getType() == Material.WALL_SIGN
                    || e.getClickedBlock().getType() == Material.SIGN_POST) {
                PreferenceMenu menu = signMenus.get(e.getClickedBlock().getLocation());
                if (menu != null) {
                    menu.open(e.getPlayer());
                    currentMenus.put(e.getPlayer().getUniqueId(), menu);
                }
            }
        }
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        PreferenceMenu menu = currentMenus.get(e.getWhoClicked().getUniqueId());
        if (menu != null) {
            e.setCancelled(true);
            if (e.getSlot() == e.getRawSlot() && e.getCurrentItem() != null
                    && e.getCurrentItem().getType() != Material.AIR) {
                menu.selectItem((Player) e.getWhoClicked(), e.getSlot());
                e.getWhoClicked().closeInventory();
                currentMenus.remove(e.getWhoClicked().getUniqueId());
            }
        }
    }
}
