package net.coasterman10.rangers.menu;

import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.kits.ItemStackBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class BanditSecondaryMenu implements Menu {
    @Override
    public String getTitle() {
        return "Select Bandit Secondary Weapon";
    }

    @Override
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, getTitle());
        inv.addItem(new ItemStackBuilder(Material.BOW).setDisplayName("Crossbow").build());
        inv.addItem(new ItemStackBuilder(Material.DIAMOND_SPADE).setDisplayName("Mace").build());
        player.openInventory(inv);
    }

    @Override
    public void selectItem(Player player, int index) {
        GamePlayer data = PlayerManager.getPlayer(player);
        data.setUpgradeSelection("bandit.secondary", index == 1 ? "mace" : "bow");
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "Selected secondary Bandit weapon: " + (index == 1 ? "Mace" : "Crossbow"));
    }
}