package net.coasterman10.rangers;

import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.game.GameTeam;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
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

    public static ItemStack getHead(Player player) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(player.getName());
        GamePlayer gp = PlayerManager.getPlayer(player);
        StringBuilder name = new StringBuilder(32);
        name.append("Head of ");
        GameTeam team = gp.getTeam();
        if (team != null)
            name.append(team.getChatColor());
        name.append(player.getName());
        if (team != null)
            if (gp.isBanditLeader())
                name.append(" (Bandit Leader)");
            else
                name.append(" (").append(team.getName().substring(0, team.getName().length() - 1)).append(")");
        meta.setDisplayName(name.toString());
        head.setItemMeta(meta);
        return head;
    }
}
