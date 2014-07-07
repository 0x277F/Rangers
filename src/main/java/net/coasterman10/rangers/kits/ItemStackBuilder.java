package net.coasterman10.rangers.kits;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;

public class ItemStackBuilder {
    private ItemStack item;
    
    public ItemStackBuilder(Material type) {
        this(type, 1);
    }
    
    public ItemStackBuilder(Material type, int amount) {
        this(type, amount, (short) 0);
    }
    
    public ItemStackBuilder(Material type, int amount, short damage) {
        this(new ItemStack(type, amount, damage));
    }
    
    public ItemStackBuilder(ItemStack item) {
        this.item = item;
    }
    
    public ItemStackBuilder setDisplayName(String displayName) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return this;
    }
    
    public ItemStackBuilder setLore(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
        return this;
    }
    
    public ItemStackBuilder addLore(String line) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        lore.add(line);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return this;
    }
    
    public ItemStackBuilder setColor(DyeColor color) {
        return setColor(color.getColor());
    }
    
    public ItemStackBuilder setColor(Color color) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(color);
            item.setItemMeta(meta);
            return this;
        }
        MaterialData data = item.getData();
        if (data instanceof Colorable) {
           ((Colorable) data).setColor(DyeColor.getByColor(color));
           item.setData(data);
        }
        return this;
    }
    
    public ItemStackBuilder addEnchantment(Enchantment type, int level) {
        item.addEnchantment(type, level);
        return this;
    }
    
    public ItemStack build() {
        return item;
    }
}
