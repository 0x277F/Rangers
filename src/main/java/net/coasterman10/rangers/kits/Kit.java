package net.coasterman10.rangers.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class Kit {
    private ItemStack[] armor = new ItemStack[4];
    private List<ItemStack> inventory = new ArrayList<>();

    public void load(ConfigurationSection config) {
        ItemStack helmet = parseItemStack(config.getString("helmet"));
        ItemStack chestplate = parseItemStack(config.getString("chestplate"));
        ItemStack leggings = parseItemStack(config.getString("leggings"));
        ItemStack boots = parseItemStack(config.getString("boots"));
        
        armor[0] = boots;
        armor[1] = leggings;
        armor[2] = chestplate;
        armor[3] = helmet;
        
        for (int i = 0; i < 36; i++) {
            ItemStack item = parseItemStack(config.getString("items." + i, null));
            inventory.add(item);
        }
    }

    public void apply(Player p) {
        p.getInventory().setArmorContents(armor);
        for (ItemStack item : inventory)
            if (item != null)
                p.getInventory().addItem(item);
    }

    @SuppressWarnings("deprecation")
    // Because of this type id magic number hysteria
    protected static final ItemStack parseItemStack(String s) {
        if (s == null)
            return null;
        
        String[] tokens = s.split(" ");

        // Format: id:amount:data enchantId:level name:"name" color:hexColor [...]

        String[] split = tokens[0].split(":");
        int id = 0;
        int amount = 0;
        short data = 0;
        try {
            id = Integer.valueOf(split[0]);
            if (split.length >= 2)
                amount = Integer.valueOf(split[1]);
            if (split.length >= 3)
                data = Short.valueOf(split[2]);
        } catch (NumberFormatException e) {
            return null;
        }

        ItemStack item = new ItemStack(id, amount, data);

        for (int i = 1; i < tokens.length; i++) {
            String[] token = tokens[i].split(":");

            if (token[0].equals("name")) {
                if (tokens.length >= 2) {
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(tokens[1].replace("\"", ""));
                    item.setItemMeta(meta);
                } else {
                    continue;
                }
            } else if (tokens[0].equals("color")) {
                if (tokens.length >= 2) {
                    if (item.getItemMeta() instanceof LeatherArmorMeta) {
                        try {
                            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
                            meta.setColor(Color.fromRGB(Integer.valueOf(tokens[1])));
                            item.setItemMeta(meta);
                        } catch (NumberFormatException e) {
                            continue;
                        }
                    }
                } else {
                    continue;
                }
            } else {
                int enchantId = 0;
                int enchantLevel = 1;
                try {
                    enchantId = Integer.valueOf(token[0]);
                    if (token.length >= 2)
                        enchantLevel = Integer.valueOf(token[1]);
                } catch (NumberFormatException e) {
                    continue;
                }
                item.addEnchantment(Enchantment.getById(enchantId), enchantLevel);
            }
        }

        return item;
    }
}
