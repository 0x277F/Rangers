package net.coasterman10.rangers.listeners;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.PlayerUtil;
import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.game.Game;
import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.game.GameTeam;
import net.coasterman10.spectate.SpectateAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.ProjectileSource;

public class PlayerListener implements Listener {
    private final Rangers plugin;

    private Collection<Material> allowedDrops = new HashSet<>();

    public PlayerListener(Rangers plugin) {
        this.plugin = plugin;
    }

    public void setAllowedDrops(Collection<Material> allowedDrops) {
        this.allowedDrops = allowedDrops;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().sendMessage("Welcome to Rangers!");
        e.getPlayer().teleport(plugin.getLobbySpawn());
        PlayerUtil.resetPlayer(e.getPlayer());

        // Get it through to players this is a dev server
        for (int i = 0; i < 10; i++) {
            e.getPlayer().sendMessage(
                    ChatColor.YELLOW + "" + ChatColor.BOLD + ChatColor.ITALIC
                            + "THIS IS A DEV SERVER, EXPECT THINGS TO BREAK");
        }
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
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (e.getClickedBlock().getState() instanceof Sign) {
            Sign s = (Sign) e.getClickedBlock().getState();
            if (s.getLine(1).equalsIgnoreCase("back to") && s.getLine(2).equalsIgnoreCase("lobby")) {
                plugin.sendToLobby(e.getPlayer());
            }
            if (s.getLine(1).toLowerCase().contains("click here") && s.getLine(2).toLowerCase().contains("to spectate")) {
                SpectateAPI.addSpectator(e.getPlayer());
            }
        } else {
            if (SpectateAPI.isSpectator(e.getPlayer()))
                SpectateAPI.removeSpectator(e.getPlayer());
            if (e.getClickedBlock().getType() == Material.CHEST && e.getPlayer().getItemInHand() != null
                    && e.getPlayer().getItemInHand().getType() == Material.SKULL_ITEM) {
                ((Chest) e.getClickedBlock().getState()).getBlockInventory().addItem(e.getPlayer().getItemInHand());
                e.getPlayer().getInventory().remove(e.getPlayer().getItemInHand());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        GamePlayer player = PlayerManager.getPlayer(e.getEntity());

        // Huge mess of code to generate death message. Don't ask.
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
                        boolean foundBow = false;
                        for (ItemStack item : ((Player) shooter).getInventory()) {
                            if (item == null)
                                continue;
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
                                foundBow = true;
                                break;
                            }
                        }
                        if (!foundBow) {
                            msg.append(ChatColor.DARK_RED).append(" using their ");
                            msg.append(ChatColor.YELLOW).append("BARE HANDS!");
                        }
                    }
                } else if (damager instanceof Item) {
                    ItemStack item = ((Item) damager).getItemStack();
                    if (item.getType() == Material.TRIPWIRE_HOOK) {
                        msg.append(ChatColor.DARK_RED).append(" was killed by ");
                        String shooter = null;
                        List<MetadataValue> metadata = damager.getMetadata("shooter");
                        for (MetadataValue value : metadata) {
                            if (value.getOwningPlugin().equals(plugin)) {
                                shooter = value.asString();
                                break;
                            }
                        }
                        // Since it's fairly messy to deal with them offline I'm just hardcoding in that they
                        // are a Ranger (since only Rangers get the throwing knife anyway)
                        msg.append(GameTeam.RANGERS.getChatColor()).append(shooter);
                        msg.append("(").append(GameTeam.RANGERS.getName()).append(")");
                        msg.append(ChatColor.DARK_RED).append(" using a ").append(ChatColor.YELLOW);
                        msg.append("Throwing Knife");
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

            // Put them in spectator mode
            SpectateAPI.addSpectator(e.getEntity());
        } else {
            e.getDrops().clear(); // There should be no drops at all outside of the game
            e.setDeathMessage(null);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        GamePlayer player = PlayerManager.getPlayer(e.getPlayer());
        Game g = player.getGame();
        if (g == null)
            e.setRespawnLocation(plugin.getLobbySpawn());
        else if (player.getTeam() != null)
            e.setRespawnLocation(g.getArena().getMap().getSpawn(player.getTeam()).addTo(g.getArena().getOrigin()));
        else
            e.setRespawnLocation(g.getArena().getLobbySpawn());
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
        } else if (e.getItem().getType() == EntityType.DROPPED_ITEM
                && !allowedDrops.contains(e.getItem().getItemStack().getType())) {
            GamePlayer player = PlayerManager.getPlayer(e.getPlayer());
            if (player.getGame() != null && player.getGame().isRunning())
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        if (PlayerManager.getPlayer(e.getPlayer()).getGame() != null) {
            if (!allowedDrops.contains(e.getItemDrop().getItemStack().getType()))
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (PlayerManager.getPlayer(e.getPlayer()).getGame() != null || !e.getPlayer().isOp()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (PlayerManager.getPlayer(e.getPlayer()).getGame() != null || !e.getPlayer().isOp()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Game g = PlayerManager.getPlayer((Player) e.getEntity()).getGame();
            if (g == null || (g != null && !g.allowPvp()))
                e.setCancelled(true);
            if (PlayerManager.getPlayer((Player) e.getEntity()).getTeam() == (PlayerManager.getPlayer((Player) e
                    .getDamager()).getTeam()))
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
