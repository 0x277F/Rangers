package net.coasterman10.rangers.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.game.GameTeam;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class HeadDropListener implements Listener {
    private Map<UUID, Location> safeLocations = new HashMap<>();
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent e) {
        Location loc = e.getTo();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        for (int y = loc.getBlockY(); y >= 0; y--) {
            if (loc.getWorld().getBlockAt(x, y, z).getType().isSolid()) {
                safeLocations.put(e.getPlayer().getUniqueId(), loc);
                break;
            }
        }
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        GamePlayer player = PlayerManager.getPlayer(e.getEntity());
        if (player.getGame() != null && player.isAlive()) {
            Location drop = safeLocations.get(player.id);
            drop.getWorld().dropItem(drop, getHead(e.getEntity()));
        }
    }
    
    private static ItemStack getHead(Player player) {
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
