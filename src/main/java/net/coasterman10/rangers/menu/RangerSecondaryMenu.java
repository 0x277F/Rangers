package net.coasterman10.rangers.menu;

import net.coasterman10.rangers.kits.ItemStackBuilder;
import net.coasterman10.rangers.player.PlayerData;
import net.coasterman10.rangers.player.RangersPlayer;
import net.coasterman10.rangers.util.SignText;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class RangerSecondaryMenu implements PreferenceMenu {
    @Override
    public String getTitle() {
        return "Select Ranger Secondary Weapon";
    }
    
    @Override
    public SignText getSignText() {
        return new SignText(new String[] { "Ranger", "Secondary:" , "", ""});
    }

    @Override
    public String getPreferenceKey() {
        return "ranger.secondary";
    }

    @Override
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, getTitle());
        inv.addItem(new ItemStackBuilder(Material.TRIPWIRE_HOOK).setDisplayName("Throwing Knife").build());
        inv.addItem(new ItemStackBuilder(Material.SLIME_BALL).setDisplayName("Strikers").build());
        player.openInventory(inv);
    }

    @Override
    public void selectItem(Player player, int index) {
        PlayerData data = RangersPlayer.getPlayer(player).getData();
        data.setUpgradeSelection(getPreferenceKey(), index == 1 ? "strikers" : "throwingknife");
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "Selected Secondary Ranger Weapon: "
                + (index == 1 ? "Strikers" : "Throwing Knife"));
    }
}
