package net.coasterman10.rangers.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.menu.PreferenceMenu;
import net.coasterman10.rangers.util.SignText;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.blablubbabc.insigns.InSigns;
import de.blablubbabc.insigns.SignSendEvent;

public class MenuManager implements Listener {
    private static final Map<String, String> upgradeNames = new HashMap<>();
   
    // TODO: Cleaner upgrade system
    static {
        upgradeNames.put("none", "None");
        upgradeNames.put("cloak", "Cloak");
        upgradeNames.put("throwingknife", "Throwing Knife");
        upgradeNames.put("strikers", "Strikers");
        upgradeNames.put("bow", "Bow");
        upgradeNames.put("mace", "Mace");
        upgradeNames.put("grapple", "Grapple");
        upgradeNames.put("8arrows", "+8 Arrows");
        upgradeNames.put("16arrows", "+16 Arrows");
    }
    
    private Map<SignText, PreferenceMenu> signMenus = new HashMap<>();
    private Map<UUID, PreferenceMenu> currentMenus = new HashMap<>();
    private Map<UUID, Sign> clickedSigns = new HashMap<>();

    public void addSignMenu(SignText text, PreferenceMenu menu) {
        signMenus.put(text, menu);
    }

    @EventHandler
    public void onSignSend(SignSendEvent e) {
        PreferenceMenu menu = getMenu(e.getLocation().getBlock());
        if (menu != null) {
            SignText text = menu.getSignText();
            e.setLine(0, text.getLine(0));
            e.setLine(1, text.getLine(1));
            String selection = PlayerManager.getPlayer(e.getPlayer()).getUpgradeSelection(menu.getPreferenceKey());
            String friendlySelection = upgradeNames.get(selection);
            e.setLine(2, ChatColor.GREEN + (friendlySelection != null ? friendlySelection : selection));
            e.setLine(3, "Click To Change");
        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        PreferenceMenu menu = getMenu(e.getClickedBlock());
        if (menu != null) {
            menu.open(e.getPlayer());
            currentMenus.put(e.getPlayer().getUniqueId(), menu);
            clickedSigns.put(e.getPlayer().getUniqueId(), (Sign) e.getClickedBlock().getState());
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
                InSigns.sendSignChange((Player) e.getWhoClicked(), clickedSigns.remove(e.getWhoClicked().getUniqueId()));
            }
        }
    }

    private PreferenceMenu getMenu(Block block) {
        if (block != null && (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)) {
            return signMenus.get(new SignText(((Sign) block.getState()).getLines()));
        } else {
            return null;
        }
    }
}
