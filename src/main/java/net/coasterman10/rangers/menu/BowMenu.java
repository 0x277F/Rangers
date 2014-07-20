package net.coasterman10.rangers.menu;

import net.coasterman10.rangers.GamePlayer;
import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.kits.ItemStackBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class BowMenu implements Menu {
    @Override
    public String getTitle() {
        return "Select Bow Upgrades";
    }

    @Override
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, getTitle());
        inv.addItem(new ItemStackBuilder(Material.BOW).setDisplayName("None").build());
        inv.addItem(new ItemStackBuilder(Material.BOW).addEnchantment(Enchantment.ARROW_FIRE, 1)
                .setDisplayName("Flame").build());
        player.openInventory(inv);
    }

    @Override
    public void selectItem(Player player, int index) {
        GamePlayer data = PlayerManager.getPlayer(player);
        data.setUpgradeSelection("ranger.bow", index == 1 ? "flamelongbow" : "longbow");
        data.setUpgradeSelection("bandit.bow", index == 1 ? "flamecrossbow" : "crossbow");
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "Selected bow upgrade: " + (index == 1 ? "Flame" : "None"));
    }
}
