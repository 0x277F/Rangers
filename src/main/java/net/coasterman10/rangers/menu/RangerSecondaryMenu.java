package net.coasterman10.rangers.menu;

import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.kits.ItemStackBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class RangerSecondaryMenu implements Menu {
    @Override
    public String getTitle() {
        return "Select Ranger Secondary Weapon";
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
        GamePlayer data = PlayerManager.getPlayer(player);
        data.setUpgradeSelection("ranger.secondary", index == 1 ? "strikers" : "throwingknife");
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "Selected secondary Ranger weapon: "
                + (index == 1 ? "Strikers" : "Throwing Knife"));
    }
}
