package net.coasterman10.rangers.listeners;

import java.util.Collection;
import java.util.HashSet;

import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.game.GameState;
import net.coasterman10.rangers.player.RangersPlayer;
import net.coasterman10.rangers.player.RangersPlayer.PlayerState;
import net.coasterman10.spectate.SpectateAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.meta.SkullMeta;

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
        e.getPlayer().sendMessage(ChatColor.GREEN + "Welcome to Rangers!");
        e.getPlayer().teleport(plugin.getLobbySpawn());
        RangersPlayer.getPlayer(e.getPlayer()).resetPlayer();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (SpectateAPI.isSpectator(e.getPlayer())) {
            RangersPlayer player = RangersPlayer.getPlayer(e.getPlayer());
            if (player.isInArena()) {
                SpectateAPI.removeSpectator(e.getPlayer());
                player.teleport(player.getArena().getLobbySpawn());
            }
        }
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (e.getClickedBlock().getState() instanceof Sign) {
            Sign s = (Sign) e.getClickedBlock().getState();
            if (s.getLine(1).equalsIgnoreCase("back to") && s.getLine(2).equalsIgnoreCase("lobby")) {
                plugin.sendToLobby(e.getPlayer());
            }
            if (s.getLine(1).toLowerCase().contains("click here") && s.getLine(2).toLowerCase().contains("to spectate")) {
                RangersPlayer player = RangersPlayer.getPlayer(e.getPlayer());
                if (player.isInArena()) {
                    if (player.getArena().getState() == GameState.RUNNING) {
                        SpectateAPI.addSpectator(e.getPlayer());
                        player.teleport(player.getArena().getSpectatorSpawn());
                        e.getPlayer().setAllowFlight(true);
                        e.getPlayer().setFlying(true);
                        e.getPlayer().sendMessage(
                                ChatColor.DARK_AQUA
                                        + "You are now spectating the match. Click anywhere to return to the lobby.");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
//        RangersPlayer player = RangersPlayer.getPlayer(e.getEntity());
//
//        if (!player.isPlaying()) {
//            e.setDeathMessage(null);
//            return;
//        }
//
//        // Huge mess of code to generate death message. Don't ask.
//        StringBuilder msg = new StringBuilder(64);
//        msg.append(player.getType().getChatColor()).append(e.getEntity().getName());
//        msg.append("(").append(player.getType().getName()).append(")");
//        EntityDamageEvent cause = e.getEntity().getLastDamageCause();
//        if (cause instanceof EntityDamageByEntityEvent) {
//            Entity damager = ((EntityDamageByEntityEvent) cause).getDamager();
//            if (damager instanceof Player) {
//                msg.append(ChatColor.DARK_RED).append(" was slain by ");
//                RangersPlayer attacker = RangersPlayer.getPlayer((Player) damager);
//                if (attacker.getTeam() == RangersTeam.BANDITS)
//                    attacker.getBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 2));
//                msg.append(attacker.getType().getChatColor()).append(((Player) damager).getName());
//                msg.append("(").append(attacker.getType().getName()).append(")");
//                ItemStack item = ((Player) damager).getItemInHand();
//                if (item != null) {
//                    msg.append(ChatColor.DARK_RED).append(" using a ").append(ChatColor.YELLOW);
//                    String itemName = item.hasItemMeta() ? (item.getItemMeta().hasDisplayName() ? item.getItemMeta()
//                            .getDisplayName() : null) : null;
//                    if (itemName != null) {
//                        msg.append(itemName);
//                    } else {
//                        String typeName = item.getType().name();
//                        msg.append(typeName.substring(0, 1).toUpperCase()).append(typeName.substring(1).toLowerCase());
//                    }
//                } else {
//                    msg.append(ChatColor.DARK_RED).append(" using their ");
//                    msg.append(ChatColor.YELLOW).append("BARE HANDS!");
//                }
//            } else if (damager instanceof Arrow) {
//                ProjectileSource shooter = ((Arrow) damager).getShooter();
//                if (shooter instanceof Player) {
//                    msg.append(ChatColor.DARK_RED).append(" was shot by ");
//                    RangersPlayer attacker = RangersPlayer.getPlayer((Player) shooter);
//                    msg.append(attacker.getType().getChatColor()).append(((Player) shooter).getName());
//                    msg.append("(").append(attacker.getType().getName()).append(")");
//                    boolean foundBow = false;
//                    for (ItemStack item : ((Player) shooter).getInventory()) {
//                        if (item == null)
//                            continue;
//                        if (item.getType() == Material.BOW) {
//                            msg.append(ChatColor.DARK_RED).append(" using a ").append(ChatColor.YELLOW);
//                            String itemName = item.getItemMeta().getDisplayName();
//                            if (itemName != null) {
//                                msg.append(itemName);
//                            } else {
//                                String typeName = item.getType().name();
//                                msg.append(typeName.substring(0, 1).toUpperCase()).append(
//                                        typeName.substring(1).toLowerCase());
//                            }
//                            foundBow = true;
//                            break;
//                        }
//                    }
//                    if (!foundBow) {
//                        msg.append(ChatColor.DARK_RED).append(" using their ");
//                        msg.append(ChatColor.YELLOW).append("BARE HANDS!");
//                    }
//                }
//            } else if (damager instanceof Item) {
//                ItemStack item = ((Item) damager).getItemStack();
//                if (item.getType() == Material.TRIPWIRE_HOOK) {
//                    msg.append(ChatColor.DARK_RED).append(" was killed by ");
//                    String shooter = null;
//                    List<MetadataValue> metadata = damager.getMetadata("shooter");
//                    for (MetadataValue value : metadata) {
//                        if (value.getOwningPlugin().equals(plugin)) {
//                            shooter = value.asString();
//                            break;
//                        }
//                    }
//                    // Since it's fairly messy to deal with them offline I'm just hardcoding in that they
//                    // are a Ranger (since only Rangers get the throwing knife anyway)
//                    msg.append(RangersTeam.RANGERS.getChatColor()).append(shooter);
//                    msg.append("(").append(RangersTeam.RANGERS.getName()).append(")");
//                    msg.append(ChatColor.DARK_RED).append(" using a ").append(ChatColor.YELLOW);
//                    msg.append("Throwing Knife");
//                }
//            } else {
//                msg.append(ChatColor.DARK_RED + " was killed");
//            }
//        } else if (cause.getCause() == DamageCause.FALL) {
//            msg.append(ChatColor.DARK_RED + " hit the ground too hard");
//        } else {
//
//            // TODO: Fill in alternate death reasons
//            msg.append(ChatColor.DARK_RED + " died");
//        }
//        e.setDeathMessage(msg.toString());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        RangersPlayer player = RangersPlayer.getPlayer(e.getPlayer());
        if (player.isInArena()) {
            e.setRespawnLocation(player.getArena().getLobbySpawn());
            player.setCanDoubleJump(false);
            player.setState(PlayerState.GAME_LOBBY);
        } else {
            e.setRespawnLocation(plugin.getLobbySpawn());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent e) {
        RangersPlayer.getPlayer(e.getPlayer()).updateSafeLocation();
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent e) {
        if (e.getItem().getItemStack().getType() == Material.SKULL_ITEM) {
            SkullMeta meta = (SkullMeta) e.getItem().getItemStack().getItemMeta();
            if (meta.hasOwner()) {
                @SuppressWarnings("deprecation")
                Player owner = Bukkit.getPlayer(meta.getOwner());
                if (owner != null) {
                    RangersPlayer ownerPlayer = RangersPlayer.getPlayer(owner);
                    RangersPlayer pickupPlayer = RangersPlayer.getPlayer(e.getPlayer());
                    if (ownerPlayer.getTeam() == pickupPlayer.getTeam()) {
                        e.setCancelled(true);
                    }
                }
            }
        } else if (!allowedDrops.contains(e.getItem().getItemStack().getType())) {
            RangersPlayer player = RangersPlayer.getPlayer(e.getPlayer());
            if (player.isInArena()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        if (RangersPlayer.getPlayer(e.getPlayer()).isInArena()) {
            if (!allowedDrops.contains(e.getItemDrop().getItemStack().getType())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (RangersPlayer.getPlayer(e.getPlayer()).isPlaying() || !e.getPlayer().isOp()
                || !e.getPlayer().hasPermission("rangers.arena.build")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (RangersPlayer.getPlayer(e.getPlayer()).isPlaying() || !e.getPlayer().isOp()
                || !e.getPlayer().hasPermission("rangers.arena.build")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            if (!RangersPlayer.getPlayer((Player) e.getEntity()).isPlaying()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if (!RangersPlayer.getPlayer((Player) e.getEntity()).isPlaying()) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            if (!RangersPlayer.getPlayer((Player) e.getEntity()).isPlaying()) {
                e.setFoodLevel(20);
            }
        }
    }
}
