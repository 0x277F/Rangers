package net.coasterman10.rangers.listeners;

import java.util.Collection;
import java.util.HashSet;
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
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;

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
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent e) {
        GamePlayer player = PlayerManager.getPlayer(e.getPlayer());
        if (player.getGame() != null) {
            player.getGame().removePlayer(player);
        }
        PlayerManager.removePlayer(e.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (SpectateAPI.isSpectator(e.getPlayer())) {
            GamePlayer player = PlayerManager.getPlayer(e.getPlayer());
            if (player.getGame() != null) {
                SpectateAPI.removeSpectator(e.getPlayer());
                player.getGame().getArena().sendToLobby(player);
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
                GamePlayer player = PlayerManager.getPlayer(e.getPlayer());
                if (player.getGame() != null) {
                    SpectateAPI.addSpectator(e.getPlayer());
                    player.getGame().getArena().sendSpectatorToGame(player);
                    e.getPlayer().setAllowFlight(true);
                    e.getPlayer().setFlying(true);
                    e.getPlayer().sendMessage(
                            ChatColor.DARK_AQUA
                                    + "You are now spectating the match. Click anywhere to return to the lobby.");
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        GamePlayer player = PlayerManager.getPlayer(e.getEntity());

        if (!player.isAlive()) {
            e.setDeathMessage(null);
            return;
        }

        // Huge mess of code to generate death message. Don't ask.
        StringBuilder msg = new StringBuilder(64);
        ChatColor teamColor = (player.getTeam() != null ? player.getTeam().getChatColor() : ChatColor.WHITE);
        msg.append(teamColor).append(e.getEntity().getName());
        if (player.isBanditLeader())
            msg.append("(Bandit Leader)");
        else
            msg.append("(").append(player.getTeam().getName()).append(")");
        EntityDamageEvent cause = e.getEntity().getLastDamageCause();
        if (cause instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) cause).getDamager();
            if (damager instanceof Player) {
                msg.append(ChatColor.DARK_RED).append(" was slain by ");
                GamePlayer attacker = PlayerManager.getPlayer((Player) damager);
                if (attacker.getTeam() == GameTeam.BANDITS)
                    attacker.getHandle().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 2));
                msg.append(attacker.getTeam().getChatColor()).append(((Player) damager).getName());
                if (attacker.isBanditLeader())
                    msg.append("(Bandit Leader)");
                else
                    msg.append("(").append(attacker.getTeam().getName()).append(")");
                ItemStack item = ((Player) damager).getItemInHand();
                if (item != null) {
                    msg.append(ChatColor.DARK_RED).append(" using a ").append(ChatColor.YELLOW);
                    String itemName = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : null;
                    if (itemName != null) {
                        msg.append(itemName);
                    } else {
                        String typeName = item.getType().name();
                        msg.append(typeName.substring(0, 1).toUpperCase()).append(typeName.substring(1).toLowerCase());
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
                    if (attacker.isBanditLeader())
                        msg.append("(Bandit Leader)");
                    else
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
        } else if (cause.getCause() == DamageCause.FALL) {
            msg.append(ChatColor.DARK_RED + " hit the ground too hard");
        } else {

            // TODO: Fill in alternate death reasons
            msg.append(ChatColor.DARK_RED + " died");
        }
        e.setDeathMessage(msg.toString());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        final GamePlayer player = PlayerManager.getPlayer(e.getPlayer());
        if (player.isInGame()) {
            e.setRespawnLocation(player.getGame().getArena().getLobby());
            player.setAlive(false);
        } else {
            e.setRespawnLocation(plugin.getLobbySpawn());
        }

        // Do this in a later tick because Bukkit sucks
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setCanDoubleJump(false);
            }
        }.runTask(plugin);
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
        } else if (!allowedDrops.contains(e.getItem().getItemStack().getType())) {
            GamePlayer player = PlayerManager.getPlayer(e.getPlayer());
            if (player.isInGame() && player.isAlive())
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
        if (PlayerManager.getPlayer(e.getPlayer()).getGame() != null || !e.getPlayer().isOp()
                || !e.getPlayer().hasPermission("rangers.arena.build")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (PlayerManager.getPlayer(e.getPlayer()).getGame() != null || !e.getPlayer().isOp()
                || !e.getPlayer().hasPermission("rangers.arena.build")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            GamePlayer player = PlayerManager.getPlayer((Player) e.getEntity());
            if (player.getGame() == null || !player.getGame().allowPvp() || !player.isAlive())
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Game g = PlayerManager.getPlayer((Player) e.getEntity()).getGame();
            if (g == null || !g.allowPvp())
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTag(AsyncPlayerReceiveNameTagEvent e) {
        if (PlayerManager.isInGame(e.getPlayer()) && PlayerManager.isInGame(e.getNamedPlayer())
                && !e.getPlayer().canSee(e.getNamedPlayer())
                && !e.getPlayer().getNearbyEntities(10, 10, 10).contains(e.getNamedPlayer())) {
            e.setTag("§§§§");
        }
    }
}
