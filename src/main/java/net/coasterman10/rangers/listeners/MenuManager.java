package net.coasterman10.rangers.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.coasterman10.rangers.SignText;
import net.coasterman10.rangers.menu.Menu;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class MenuManager implements Listener {
    private Map<String, Menu> menus = new HashMap<>();
    private Map<SignText, Menu> signMenus = new HashMap<>();

    public void addMenu(Menu menu) {
        menus.put(menu.getTitle(), menu);
    }

    public void addSignMenu(Menu menu, SignText text) {
        addMenu(menu);
        signMenus.put(text, menu);
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null) {
            if (e.getClickedBlock().getType() == Material.WALL_SIGN
                    || e.getClickedBlock().getType() == Material.SIGN_POST) {
                Sign s = (Sign) e.getClickedBlock().getState();
                for (Entry<SignText, Menu> menu : signMenus.entrySet()) {
                    if (menu.getKey().matches(s.getLines())) {
                        menu.getValue().open(e.getPlayer());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        if (menus.containsKey(e.getInventory().getTitle())) {
            e.setCancelled(true);
            if (e.getSlot() == e.getRawSlot() && e.getCurrentItem() != null) {
                menus.get(e.getInventory().getTitle()).selectItem((Player) e.getWhoClicked(), e.getSlot());
            }
        }
    }
}
