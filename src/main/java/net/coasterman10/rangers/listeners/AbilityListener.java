package net.coasterman10.rangers.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.game.GameTeam;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class AbilityListener implements Listener {
    private final Plugin plugin;
    private Set<UUID> throwingKnifeCooldowns = new HashSet<>();
    private Set<UUID> doubleJumpers = new HashSet<>();
    private Set<UUID> futureSneak = new HashSet<>();
    private Map<UUID, BukkitTask> sneakTasks = new HashMap<>();

    public AbilityListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof LivingEntity && e.getDamager() instanceof Player) {
            ItemStack item = ((Player) e.getDamager()).getItemInHand();
            if (item.getType() == Material.DIAMOND_SPADE) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName() && meta.getDisplayName().contains("Mace")) {
                    // 30% nausea I
                    if (new Random().nextDouble() < 0.6) {
                        ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,
                                100, 0));
                    }
                }
            }
        }

        if (e.getEntity() instanceof Player) {
            GamePlayer player = PlayerManager.getPlayer((Player) e.getEntity());
            if (player.isVanished())
                player.unvanish();
        }

        if (e.getDamager() instanceof Player) {
            GamePlayer player = PlayerManager.getPlayer((Player) e.getDamager());
            if (player.isVanished())
                player.unvanish();
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent e) {
        if (e.isFlying() && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            Player p = e.getPlayer();
            GamePlayer player = PlayerManager.getPlayer(p);
            if (player.getGame() != null && player.canDoubleJump()) {
                player.doubleJump(plugin);
                doubleJumpers.add(player.id);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getCause() == DamageCause.FALL && doubleJumpers.contains(e.getEntity().getUniqueId())) {
            e.setCancelled(true);
        }

        if (e.getEntity() instanceof Player) {
            GamePlayer player = PlayerManager.getPlayer((Player) e.getEntity());
            if (player.isVanished())
                player.unvanish();
        }
    }

    @EventHandler
    public void onFireBow(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player) {
            GamePlayer player = PlayerManager.getPlayer((Player) e.getEntity());
            if (player.isVanished())
                player.unvanish();
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() != null) {
            if (e.getItem().getType() == Material.TRIPWIRE_HOOK) {
                final Player player = e.getPlayer();

                // 5 second cooldown
                if (!throwingKnifeCooldowns.contains(player.getUniqueId())) {
                    throwingKnifeCooldowns.add(player.getUniqueId());
                    Location eye = player.getEyeLocation();
                    final Entity knife = player.getWorld().dropItem(eye, e.getItem());
                    knife.setVelocity(eye.getDirection().multiply(1.3));

                    // We need to know who shot the knife in case it kills the victim
                    knife.setMetadata("shooter", new FixedMetadataValue(plugin, player.getName()));

                    // Hit detection - because I do not feel like using NMS to create my own entity
                    new BukkitRunnable() {
                        private static final int maxTicks = 200; // Max 10 seconds
                        private static final int maxTicksOnGround = 5; // Max 0.25 seconds on ground
                        int ticks = 0;
                        int ticksOnGround = 0;

                        @Override
                        public void run() {
                            ticks++;
                            if (ticks == maxTicks) {
                                knife.remove();
                                cancel();
                                return;
                            }
                            
                            // A knife on the ground has certainly missed
                            if (knife.isOnGround()) {
                                // Slight tolerance to hitbox being on ground
                                ticksOnGround++;
                                if (ticksOnGround == maxTicksOnGround) {
                                    knife.remove();
                                    cancel();
                                    return;
                                }
                            }

                            for (Player p : Bukkit.getOnlinePlayers()) {
                                // Don't hurt the guy who threw it
                                if (player.getUniqueId().equals(p.getUniqueId()))
                                    continue;

                                // Skip anyone not in this world
                                if (!p.getWorld().equals(knife.getWorld()))
                                    continue;

                                // Prevent friendly fire
                                GameTeam t = PlayerManager.getPlayer(player).getTeam();
                                if (t != null && t.equals(PlayerManager.getPlayer(p).getTeam()))
                                    continue;

                                Location pLoc = p.getLocation();
                                Location kLoc = knife.getLocation();

                                // Check the Y first - low cost first check
                                double pY = pLoc.getY();
                                double kY = kLoc.getY();
                                if (kY >= pY && kY <= pY + 2.0) {
                                    // Check distance is less than 0.2 (distance square is faster)
                                    double distX = pLoc.getX() - kLoc.getX();
                                    double distZ = pLoc.getZ() - kLoc.getZ();
                                    double distXZsquare = distX * distX + distZ * distZ;
                                    if (distXZsquare < 0.6) {
                                        p.damage(6.0, knife);
                                        knife.remove();
                                        cancel();
                                        break;
                                    }
                                }
                            }
                        }
                    }.runTaskTimer(plugin, 0L, 1L);

                    // Remove the player's ID from the cooldown list after 5 seconds
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            throwingKnifeCooldowns.remove(player.getUniqueId());
                        }
                    }.runTaskLater(plugin, 80L);
                }
            }

            if (e.getItem().getType() == Material.SLIME_BALL) {
                Location eye = e.getPlayer().getEyeLocation();
                final Entity striker = eye.getWorld().dropItem(eye, new ItemStack(Material.SLIME_BALL, 1));
                striker.setVelocity(eye.getDirection().multiply(0.6));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // TODO: Particle effects and better sound
                        striker.getWorld().playSound(striker.getLocation(), Sound.BLAZE_HIT, 1F, 1.75F);
                        striker.remove();
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            // Skip players not in the world
                            if (!player.getWorld().equals(striker.getWorld()))
                                continue;

                            if (player.getLocation().distance(striker.getLocation()) < 3) {
                                // Rangers are the only players that can throw these, so only bandits can be damaged
                                GamePlayer data = PlayerManager.getPlayer(player);
                                if (data.getTeam() == GameTeam.BANDITS) {
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 2));
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0));
                                }
                            }
                        }
                    }
                }.runTaskLater(plugin, 40L);

                // Remove from the player's inventory
                if (e.getItem().getAmount() > 1) {
                    e.getPlayer().getItemInHand().setAmount(e.getPlayer().getItemInHand().getAmount() - 1);
                } else {
                    e.getPlayer().getInventory().remove(Material.SLIME_BALL);
                }
            }
        }
    }

    @EventHandler
    public void onSneak(final PlayerToggleSneakEvent e) {
        final GamePlayer player = PlayerManager.getPlayer(e.getPlayer());
        if (e.isSneaking()) {
            if (player.getGame() != null && player.getGame().isRunning() && player.getTeam() == GameTeam.RANGERS
                    && player.getUpgradeSelection("ranger.ability").equals("vanish")
                    && !futureSneak.contains(player.id)) {
                futureSneak.add(player.id);
                // If they are still sneaking after 3 seconds, vanish
                sneakTasks.put(player.id, new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.getHandle() != null && player.getHandle().isSneaking()) {
                            final Location initial = player.getHandle().getLocation();
                            player.vanish();
                            sneakTasks.put(player.id, new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (player.getHandle() != null) {
                                        if (player.getHandle().getLocation().distance(initial) > 1.0
                                                && player.isVanished()) {
                                            player.unvanish();
                                            cancel();
                                        }
                                    } else {
                                        cancel();
                                    }
                                }
                            }.runTaskTimer(plugin, 0L, 5L));
                        }
                        futureSneak.remove(player.id);
                    }
                }.runTaskLater(plugin, 60L));
            }
        } else {
            if (sneakTasks.containsKey(player.id)) {
                sneakTasks.remove(player.id).cancel();
            }
            futureSneak.remove(player.id);
            if (player.isVanished()) {
                player.unvanish();
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        // Bandits get regeneration II for 2 seconds when they kill a player
        if (e.getEntity().getKiller() != null) {
            if (PlayerManager.getPlayer(e.getEntity().getKiller()).getTeam() == GameTeam.BANDITS)
                e.getEntity().getKiller().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1, 40));
        }
    }
}
