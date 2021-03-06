package net.coasterman10.rangers.kits;

import net.coasterman10.rangers.player.RangersPlayer;
import net.coasterman10.rangers.player.RangersPlayer.PlayerType;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class BanditKit implements Kit {
    private static final ItemStack[] ARMOR = new ItemStack[4];
    private static final ItemStack[] BASE = new ItemStack[36];

    static {
        ARMOR[0] = new ItemStackBuilder(Material.LEATHER_BOOTS).setColor(Color.RED).build();
        ARMOR[1] = new ItemStackBuilder(Material.CHAINMAIL_LEGGINGS).build();
        ARMOR[2] = new ItemStackBuilder(Material.CHAINMAIL_CHESTPLATE).addEnchantment(
                Enchantment.PROTECTION_ENVIRONMENTAL, 2).build();
        ARMOR[3] = new ItemStackBuilder(Material.LEATHER_HELMET).setColor(Color.RED).build();

        BASE[0] = new ItemStackBuilder(Material.IRON_SWORD).addEnchantment(Enchantment.KNOCKBACK, 1)
                .setDisplayName("Shortsword").build();
        BASE[1] = new ItemStackBuilder(Material.BOW).addEnchantment(Enchantment.ARROW_KNOCKBACK, 1)
                .setDisplayName("Crossbow").build();
        BASE[2] = new ItemStack(Material.BREAD, 4);
        BASE[8] = new ItemStack(Material.ARROW, 8);
    }

    @Override
    public void apply(RangersPlayer player) {
        PlayerInventory inv = player.getBukkitPlayer().getInventory();
        inv.clear();
        inv.setContents(BASE);
        inv.setArmorContents(ARMOR);

        if (player.getData().getUpgradeSelection("bandit.secondary").equals("mace")) {
            inv.setItem(1, new ItemStackBuilder(Material.DIAMOND_SPADE).setDisplayName("Mace").build());
            inv.remove(Material.ARROW);
        } else {
            ItemStack arrows = inv.getItem(8);
            arrows.setAmount(arrows.getAmount() + 8);
        }

        if (player.getData().getUpgradeSelection("bandit.ability").equals("grapple")) {
            inv.setItem(2, new ItemStackBuilder(Material.FISHING_ROD).setDisplayName("Grapple").build());
            inv.setItem(3, new ItemStack(Material.BREAD, 4));
        }

        if (player.getType() == PlayerType.BANDIT_LEADER) {
            inv.setBoots(new ItemStack(Material.GOLD_BOOTS));
            inv.setHelmet(new ItemStack(Material.GOLD_HELMET));
            inv.setItem(0, new ItemStackBuilder(Material.DIAMOND_SWORD).addEnchantment(Enchantment.KNOCKBACK, 1)
                    .setDisplayName("Longsword").build());
        }
    }
}
