package net.coasterman10.rangers.menu;

import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.kits.ItemStackBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class BanditBowMenu implements Menu {
    @Override
    public String getTitle() {
        return "Select Bandit Bow Upgrades";
    }

    @Override
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, getTitle());
        inv.addItem(new ItemStackBuilder(Material.EYE_OF_ENDER).setDisplayName("None").build());
        inv.addItem(new ItemStackBuilder(Material.ARROW, 8).setDisplayName("+8 Arrows").build());
        player.openInventory(inv);
    }

    @Override
    public void selectItem(Player player, int index) {
        GamePlayer data = PlayerManager.getPlayer(player);
        data.setUpgradeSelection("bandit.bow", index == 1 ? "8arrows" : "none");
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "Selected bow upgrade: " + (index == 1 ? "+8 Arrows" : "None"));
    }
}
