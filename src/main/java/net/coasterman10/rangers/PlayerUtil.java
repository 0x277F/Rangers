package net.coasterman10.rangers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerUtil {
    private PlayerUtil() {

    }

    public static void resetPlayer(Player p) {
        p.setGameMode(GameMode.ADVENTURE);
        p.setHealth(p.getMaxHealth());
        p.setFoodLevel(20);
        p.setSaturation(10F);
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        p.setExp(0F);
        p.setLevel(0);
        p.setFireTicks(0);
        for (PotionEffect effect : p.getActivePotionEffects())
            p.removePotionEffect(effect.getType());
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!PlayerManager.getPlayer(other).isVanished())
                p.showPlayer(other);
            other.showPlayer(p);
        }
    }

    public static void addPermanentEffect(Player p, PotionEffectType effect, int amp) {
        p.addPotionEffect(new PotionEffect(effect, Integer.MAX_VALUE, amp));
    }
}
