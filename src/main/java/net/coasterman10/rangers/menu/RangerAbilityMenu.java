package net.coasterman10.rangers.menu;

import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.kits.ItemStackBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public class RangerAbilityMenu implements Menu {
    @Override
    public String getTitle() {
        return "Select Ranger Ability";
    }

    @Override
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, getTitle());
        inv.addItem(new ItemStackBuilder(Material.EYE_OF_ENDER).setDisplayName("Default").build());
        inv.addItem(new ItemStackBuilder(new Potion(PotionType.INVISIBILITY).toItemStack(1)).setDisplayName("Vanish")
                .build());
        player.openInventory(inv);
    }

    @Override
    public void selectItem(Player player, int index) {
        GamePlayer data = PlayerManager.getPlayer(player);
        data.setUpgradeSelection("ranger.ability", index == 1 ? "vanish" : "none");
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "Selected ability: " + (index == 1 ? "Vanish" : "None"));
    }
}
