package net.coasterman10.rangers.kits;

import net.coasterman10.rangers.GamePlayer;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class RangerKit implements Kit {
    private static final ItemStack[] ARMOR = new ItemStack[4];
    private static final ItemStack[] BASE = new ItemStack[36];

    static {
        ARMOR[0] = new ItemStackBuilder(Material.LEATHER_BOOTS).addEnchantment(Enchantment.PROTECTION_FALL, 2)
                .setColor(Color.GREEN).build();
        ARMOR[1] = new ItemStackBuilder(Material.LEATHER_LEGGINGS).setColor(Color.GREEN).build();
        ARMOR[2] = new ItemStackBuilder(Material.LEATHER_CHESTPLATE).setColor(Color.GREEN).build();
        ARMOR[3] = new ItemStackBuilder(Material.LEATHER_HELMET).setColor(Color.GREEN).build();

        BASE[0] = new ItemStackBuilder(Material.IRON_SWORD).setDisplayName("Dagger")
                .addEnchantment(Enchantment.DAMAGE_ALL, 1).build();
        BASE[1] = new ItemStackBuilder(Material.BOW).addEnchantment(Enchantment.ARROW_DAMAGE, 1)
                .setDisplayName("Longbow").build();
        BASE[2] = new ItemStackBuilder(Material.TRIPWIRE_HOOK).setDisplayName("Throwing Knife").build();
        BASE[3] = new ItemStack(Material.BREAD, 4);
        BASE[8] = new ItemStack(Material.ARROW, 16);
    }

    @Override
    public void apply(GamePlayer player) {
        PlayerInventory inv = player.getHandle().getInventory();
        inv.clear();
        inv.setContents(BASE);
        inv.setArmorContents(ARMOR);

        if (player.getUpgradeSelection("ranger.bow").equals("flamelongbow")) {
            new ItemStackBuilder(inv.getItem(1)).addEnchantment(Enchantment.ARROW_FIRE, 1).setDisplayName(
                    "Flame Longbow");
        }

        if (player.getUpgradeSelection("ranger.secondary").equals("strikers")) {
            inv.setItem(2, new ItemStackBuilder(Material.SLIME_BALL, 3).setDisplayName("Strikers").build());
        }
    }
}
