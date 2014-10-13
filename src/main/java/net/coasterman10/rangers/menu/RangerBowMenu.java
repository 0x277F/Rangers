package net.coasterman10.rangers.menu;

import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.kits.ItemStackBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class RangerBowMenu implements Menu {
    @Override
    public String getTitle() {
        return "Select Ranger Bow Upgrades";
    }

    @Override
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, getTitle());
        inv.addItem(new ItemStackBuilder(Material.EYE_OF_ENDER).setDisplayName("None").build());
        inv.addItem(new ItemStackBuilder(Material.ARROW, 16).setDisplayName("+16 Arrows").build());
        player.openInventory(inv);
    }

    @Override
    public void selectItem(Player player, int index) {
        GamePlayer data = PlayerManager.getPlayer(player);
        data.setUpgradeSelection("ranger.bow", index == 1 ? "16arrows" : "none");
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "Selected bow upgrade: " + (index == 1 ? "+16 Arrows" : "None"));
    }
}
