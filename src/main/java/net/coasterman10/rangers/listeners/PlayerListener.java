package net.coasterman10.rangers.listeners;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.coasterman10.rangers.Game;
import net.coasterman10.rangers.GamePlayer;
import net.coasterman10.rangers.GameSign;
import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.Rangers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Chest;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.projectiles.ProjectileSource;

public class PlayerListener implements Listener {
    private final Rangers plugin;

    // These are initialized to failsafe values; they will and should be changed by the time any events fire.
    private Map<Location, GameSign> signs = new HashMap<>();
    private Collection<Material> allowedDrops = Collections.emptySet();

    public PlayerListener(Rangers plugin) {
        this.plugin = plugin;
    }

    public void setAllowedDrops(Collection<Material> allowedDrops) {
        this.allowedDrops = allowedDrops;
    }

    public void setSigns(Map<Location, GameSign> signs) {
        this.signs = signs;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().sendMessage("Welcome to Rangers!");
        e.getPlayer().teleport(plugin.getLobbySpawn());
        e.getPlayer().getInventory().clear();
        e.getPlayer().getInventory().setArmorContents(null); // Essentials idiot devs still haven't figured this out
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        GamePlayer data = PlayerManager.getPlayer(e.getPlayer());
        if (data.getGame() != null) {
            data.getGame().removePlayer(data);
            e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getEyeLocation(), getHead(e.getPlayer()));
        }
        PlayerManager.removePlayer(e.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        Location loc = e.getClickedBlock().getLocation();
        if (signs.containsKey(loc)) {
            signs.get(loc).getGame().addPlayer(PlayerManager.getPlayer(e.getPlayer()));
        }
        if (e.getClickedBlock().getType() == Material.CHEST && e.getPlayer().getItemInHand() != null
                && e.getPlayer().getItemInHand().getType() == Material.SKULL_ITEM) {
            ((Chest) e.getClickedBlock().getState()).getBlockInventory().addItem(e.getPlayer().getItemInHand());
            e.getPlayer().getInventory().remove(e.getPlayer().getItemInHand());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        GamePlayer player = PlayerManager.getPlayer(e.getEntity());
        if (player.getGame() != null) {
            StringBuilder msg = new StringBuilder(64);
            msg.append(player.getTeam().getChatColor()).append(e.getEntity().getName());
            msg.append("(").append(player.getTeam().getName()).append(")");
            EntityDamageEvent cause = e.getEntity().getLastDamageCause();
            if (cause instanceof EntityDamageByEntityEvent) {
                Entity damager = ((EntityDamageByEntityEvent) cause).getDamager();
                if (damager instanceof Player) {
                    msg.append(ChatColor.DARK_RED).append(" was slain by ");
                    GamePlayer attacker = PlayerManager.getPlayer((Player) damager);
                    msg.append(attacker.getTeam().getChatColor()).append(((Player) damager).getName());
                    msg.append("(").append(attacker.getTeam().getName()).append(")");
                    ItemStack item = ((Player) damager).getItemInHand();
                    if (item != null) {
                        msg.append(ChatColor.DARK_RED).append(" using a ").append(ChatColor.YELLOW);
                        String itemName = item.getItemMeta().getDisplayName();
                        if (itemName != null) {
                            msg.append(itemName);
                        } else {
                            String typeName = item.getType().name();
                            msg.append(typeName.substring(0, 1).toUpperCase()).append(
                                    typeName.substring(1).toLowerCase());
                        }
                    } else {
                        msg.append(ChatColor.DARK_RED).append(" using their ");
                        msg.append(ChatColor.YELLOW).append("BARE HANDS!");
                    }
                } else if (damager instanceof Arrow) {
                    ProjectileSource shooter = ((Arrow) damager).getShooter();
                    if (shooter instanceof Player) {
                        msg.append(ChatColor.DARK_RED).append(" was shot by ");
                        GamePlayer attacker = PlayerManager.getPlayer((Player) shooter);
                        msg.append(attacker.getTeam().getChatColor()).append(((Player) shooter).getName());
                        msg.append("(").append(attacker.getTeam().getName()).append(")");
                        for (ItemStack item : ((Player) damager).getInventory()) {
                            if (item.getType() == Material.BOW) {
                                msg.append(ChatColor.DARK_RED).append(" using a ").append(ChatColor.YELLOW);
                                String itemName = item.getItemMeta().getDisplayName();
                                if (itemName != null) {
                                    msg.append(itemName);
                                } else {
                                    String typeName = item.getType().name();
                                    msg.append(typeName.substring(0, 1).toUpperCase()).append(
                                            typeName.substring(1).toLowerCase());
                                }
                                continue;
                            }
                        }
                        msg.append(ChatColor.DARK_RED).append(" using their ");
                        msg.append(ChatColor.YELLOW).append("BARE HANDS!");
                    }
                } else {
                    msg.append(ChatColor.DARK_RED + " was killed");
                }
            } else {
                // TODO: Fill in alternate death reasons
                msg.append(ChatColor.DARK_RED + " died");
            }
            e.setDeathMessage(msg.toString());

            // Only drop allowed items (food, possibly other items in future)
            for (Iterator<ItemStack> it = e.getDrops().iterator(); it.hasNext();)
                if (!allowedDrops.contains(it.next().getType()))
                    it.remove();

            // Drop the victim's head
            e.getDrops().add(getHead(e.getEntity()));
        } else {
            e.getDrops().clear(); // There should be no drops at all outside of the game
            e.setDeathMessage(null);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Game g = PlayerManager.getPlayer(e.getPlayer()).getGame();
        if (g == null)
            e.setRespawnLocation(plugin.getLobbySpawn());
        else
            e.setRespawnLocation(g.getLobbySpawn());
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent e) {
        if (e.getItem().getItemStack().getType() == Material.SKULL_ITEM) {
            SkullMeta meta = (SkullMeta) e.getItem().getItemStack().getItemMeta();
            if (meta.hasOwner()) {
                @SuppressWarnings("deprecation")
                Player owner = Bukkit.getPlayer(meta.getOwner());
                if (owner != null) {
                    GamePlayer ownerData = PlayerManager.getPlayer(owner);
                    GamePlayer pickupData = PlayerManager.getPlayer(e.getPlayer());
                    if (ownerData.getTeam() == pickupData.getTeam()) {
                        e.setCancelled(true);
                    }
                }
            }
        } else {
            if (!allowedDrops.contains(e.getItem().getItemStack().getType()))
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Game g = PlayerManager.getPlayer((Player) e.getEntity()).getGame();
            if (g == null || (g != null && !g.allowPvp()))
                e.setCancelled(true);
        }
    }

    private static ItemStack getHead(Player player) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(player.getName());
        head.setItemMeta(meta);
        return head;
    }
}
