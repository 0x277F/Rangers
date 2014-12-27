package net.coasterman10.rangers.menu;

import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.kits.ItemStackBuilder;
import net.coasterman10.rangers.util.SignText;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class RangerAbilityMenu implements PreferenceMenu {
    @Override
    public String getTitle() {
        return "Select Ranger Ability";
    }
    
    @Override
    public SignText getSignText() {
        return new SignText(new String[] { "Ranger", "Ability:" , "", ""});
    }
    
    @Override
    public String getPreferenceKey() {
        return "ranger.ability";
    }

    @Override
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, getTitle());
        inv.addItem(new ItemStackBuilder(Material.EYE_OF_ENDER).setDisplayName("None").build());
        inv.addItem(new ItemStackBuilder(Material.QUARTZ).setDisplayName("Cloak").build());
        player.openInventory(inv);
    }

    @Override
    public void selectItem(Player player, int index) {
        GamePlayer data = PlayerManager.getPlayer(player);
        data.setUpgradeSelection(getPreferenceKey(), index == 1 ? "cloak" : "none");
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "Selected Ranger Ability: " + (index == 1 ? "Cloak" : "None"));
    }
}
