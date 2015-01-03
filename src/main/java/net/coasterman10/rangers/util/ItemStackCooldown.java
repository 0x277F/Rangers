package net.coasterman10.rangers.util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class ItemStackCooldown extends BukkitRunnable {
    private final UUID playerId;
    private final Material type;
    private final String name;
    private int seconds;

    public ItemStackCooldown(UUID playerId, Material type, String name, int seconds) {
        this.playerId = playerId;
        this.type = type;
        this.name = name;
        this.seconds = seconds;
    }

    @Override
    public void run() {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            // Scan the entire inventory for items with the given name and type to find the target item stack
            for (ItemStack item : player.getInventory()) {
                if (item != null && item.getType() == type && item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta.hasDisplayName() && item.getItemMeta().getDisplayName().contains(name)) {
                        String suffix = seconds > 0 ? ChatColor.RED + " " + seconds : ChatColor.GREEN + " READY";
                        meta.setDisplayName(ChatColor.YELLOW + name + suffix);
                    }
                }
            }
            if (seconds == 0) {
                cancel();
            } else {
                seconds--;
            }
        } else {
            cancel();
        }
    }
}
