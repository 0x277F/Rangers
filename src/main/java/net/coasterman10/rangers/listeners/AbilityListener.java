package net.coasterman10.rangers.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import net.coasterman10.rangers.game.RangersTeam;
import net.coasterman10.rangers.player.RangersPlayer;
import net.coasterman10.rangers.util.CooldownManager;
import net.coasterman10.rangers.util.ItemStackCooldown;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class AbilityListener implements Listener {
    private static final int DOUBLE_JMUP_PERIOD = 5;
    private static final int DOUBLE_JUMP_COOLDOWN = 8;
    private static final int THROWING_KNIFE_COOLDOWN = 4;

    private final Plugin plugin;
    private Set<UUID> doubleJumpers = new HashSet<>();
    private CooldownManager doubleJumpCooldown = new CooldownManager(DOUBLE_JUMP_COOLDOWN * 1000);
    private CooldownManager throwingKnifeCooldown = new CooldownManager(THROWING_KNIFE_COOLDOWN * 1000);

    public AbilityListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFish(final PlayerFishEvent e) {
        final RangersPlayer player = RangersPlayer.getPlayer(e.getPlayer());
        if (player.isPlaying() && player.getTeam() == RangersTeam.BANDITS
                && player.getData().isUpgradeSelected("bandit.ability", "grapple")) {
            if ((e.getState() == State.FAILED_ATTEMPT && (e.getHook().isOnGround() || !e.getHook().getLocation()
                    .subtract(0, 0.5, 0).getBlock().isEmpty()))
                    || e.getState() == State.IN_GROUND) {
                Vector dist = e.getHook().getLocation().subtract(e.getPlayer().getLocation()).toVector();

                e.getPlayer().getItemInHand().setDurability((short) 0);

                if (dist.getY() < 2) {
                    e.getPlayer()
                            .sendMessage(
                                    ChatColor.RED
                                            + "The grapple was not able to grip the surface well... try grappling to somewhere higher up.");
                    e.getPlayer().getWorld().playSound(e.getHook().getLocation(), Sound.ITEM_BREAK, 0.75F, 1F);
                    return;
                }

                double dx = dist.getX();
                double dy = dist.getY() + 1;
                double dz = dist.getZ();
                double dxz = Math.sqrt(dx * dx + dz * dz);

                if (e.getPlayer().getVelocity().getY() < 0)
                    dy += 2;

                final double vxz = (dxz > 0.5 ? 0.3062 + 0.0796 * dxz - 1.3795 * Math.pow(dxz, -0.5604) - 0.0109 * dy
                        + 0.9709 * Math.pow(dy, -0.2366) : 0.0);
                final double vx = vxz * dx / dxz;
                final double vy = 0.0809 + 0.0162 * dy + 0.3852 * Math.sqrt(dy);
                final double vz = vxz * dz / dxz;

                e.getPlayer().setVelocity(new Vector(0, vy, 0));
                e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ZOMBIE_INFECT, 1F, 2F);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        e.getPlayer().setVelocity(e.getPlayer().getVelocity().setX(vx).setZ(vz));
                    }
                }.runTaskLater(plugin, 1L);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
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
                        if (e.getEntity() instanceof Player) {
                            ((Player) e.getEntity()).sendMessage(ChatColor.DARK_PURPLE
                                    + "Whoa, that mace hit me pretty hard...");
                        }
                    }
                }
            }
        }

        if (e.getEntity() instanceof Player) {
            RangersPlayer player = RangersPlayer.getPlayer((Player) e.getEntity());
            if (player.isCloaked()) {
                player.uncloak();
            }
        }

        if (e.getDamager() instanceof Player) {
            RangersPlayer player = RangersPlayer.getPlayer((Player) e.getDamager());
            if (player.isCloaked()) {
                player.uncloak();
            }
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent e) {
        if (e.isFlying() && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            Player p = e.getPlayer();
            RangersPlayer player = RangersPlayer.getPlayer(p);
            final UUID id = p.getUniqueId();
            if (player.canDoubleJump() && !doubleJumpCooldown.isCoolingDown(id)) {
                e.setCancelled(true);
                p.setFlying(false);
                p.setAllowFlight(false);
                p.setVelocity(p.getLocation().getDirection().multiply(1.3).setY(1.0));
                p.getWorld().playEffect(p.getLocation().add(0.0, 0.5, 0.0), Effect.SMOKE, 4);
                p.getWorld().playSound(p.getLocation(), Sound.ZOMBIE_INFECT, 1.0F, 2.0F);
                doubleJumpers.add(id);
                doubleJumpCooldown.startCooldown(id);

                new BukkitRunnable() {
                    float time = DOUBLE_JUMP_COOLDOWN;

                    @Override
                    public void run() {
                        Player p = Bukkit.getPlayer(id);
                        if (p != null) {
                            if (!RangersPlayer.getPlayer(p).canDoubleJump()) {
                                cancel();
                                p.setExp(0F);
                            } else {
                                if (time == 0) {
                                    cancel();
                                    p.setAllowFlight(true);
                                } else {
                                    // Animate the bar refilling
                                    p.setExp((float) (DOUBLE_JUMP_COOLDOWN - time) / (float) DOUBLE_JUMP_COOLDOWN);
                                    time -= DOUBLE_JMUP_PERIOD / 20F;
                                }
                            }
                        } else {
                            // Not much point in this if they are offline
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, (long) DOUBLE_JMUP_PERIOD);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (e.getCause() == DamageCause.FALL && doubleJumpers.contains(e.getEntity().getUniqueId())) {
            e.setCancelled(true);
            doubleJumpers.remove(e.getEntity().getUniqueId());
        }

        if (e.getEntity() instanceof Player) {
            RangersPlayer player = RangersPlayer.getPlayer((Player) e.getEntity());
            if (player.isCloaked()) {
                player.uncloak();
            }
        }
    }

    @EventHandler
    public void onFireBow(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player) {
            RangersPlayer player = RangersPlayer.getPlayer((Player) e.getEntity());
            if (player.isCloaked()) {
                player.uncloak();
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() != null) {
            if (isAbilityItem(e.getItem(), Material.TRIPWIRE_HOOK, "Throwing Knife")) {
                Player player = e.getPlayer();
                if (!throwingKnifeCooldown.isCoolingDown(player)) {
                    throwingKnifeCooldown.startCooldown(player);
                    Location eye = player.getEyeLocation();

                    // Spawn the knife and launch it in the direction in which the player is looking
                    Item knife = player.getWorld().dropItem(eye, e.getItem());
                    knife.setVelocity(eye.getDirection().multiply(1.5));

                    // Prepare the text for the death message should the knife kill a victim
                    RangersPlayer rp = RangersPlayer.getPlayer(player);
                    StringBuilder msg = new StringBuilder(32);
                    msg.append(rp.getType() != null ? rp.getType().getChatColor() : ChatColor.WHITE);
                    msg.append(player.getName());
                    if (rp.getType() != null) {
                        msg.append("(").append(rp.getType().getName()).append(")");
                    }
                    knife.setMetadata("shooter", new FixedMetadataValue(plugin, msg.toString()));

                    // Hit detection - because I do not feel like using NMS magic to create my own entity
                    new ThrowingKnifeTask(player, knife).runTaskTimer(plugin, 0L, 1L);
                }
            }

            if (isAbilityItem(e.getItem(), Material.SLIME_BALL, "Strikers")) {
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
                            if (!player.getWorld().equals(striker.getWorld()))
                                continue;
                            if (player.getLocation().distance(striker.getLocation()) < 3) {
                                // Rangers are the only players that can throw these, so only bandits can be damaged
                                if (RangersPlayer.getPlayer(player).getTeam() == RangersTeam.BANDITS) {
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

            if (isAbilityItem(e.getItem(), Material.QUARTZ, "Cloak READY")) {
                final UUID id = e.getPlayer().getUniqueId();
                RangersPlayer.getPlayer(e.getPlayer()).cloak();

                // Uncloak after 10 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Player bukkitPlayer = Bukkit.getPlayer(id);
                        if (bukkitPlayer != null) {
                            RangersPlayer.getPlayer(bukkitPlayer).uncloak();
                        }
                    }
                }.runTaskLater(plugin, 200L);

                // Cooldown using the item itself as the display for the cooldown
                new ItemStackCooldown(id, Material.QUARTZ, "Cloak", 30).runTaskTimer(plugin, 0L, 20L);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        // Bandits get regeneration II for 2 seconds when they kill a player
        Player killer = e.getEntity().getKiller();
        if (killer != null) {
            if (RangersPlayer.getPlayer(e.getEntity().getKiller()).getTeam() == RangersTeam.BANDITS) {
                killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1, 40));
            }
        }
    }

    private static class ThrowingKnifeTask extends BukkitRunnable {
        private static final int maxTicks = 400; // Max 20 seconds life
        private static final int maxTicksOnGround = 2; // Max 0.1 seconds on ground
        private static final double collisionDistanceSquared = 0.5; // Max distance squared for collision

        private final List<Player> subjects = new ArrayList<>();
        private final Item knife;
        private int ticks = 0;
        private int ticksOnGround = 0;

        private ThrowingKnifeTask(Player player, Item knife) {
            this.knife = knife;

            // The throwing knife can only affect players in the same arena as the player who threw it
            RangersPlayer rp = RangersPlayer.getPlayer(player);
            if (rp.isInArena()) {
                for (RangersPlayer arenaPlayer : rp.getArena().getPlayers()) {
                    // Prevent self-harm and friendly fire
                    if (arenaPlayer != player && rp.getTeam() != arenaPlayer.getTeam()) {
                        subjects.add(arenaPlayer.getBukkitPlayer());
                    }
                }
            }
        }

        @Override
        public void run() {
            if (ticks == maxTicks) {
                knife.remove();
                cancel();
                return;
            }
            ticks++;

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

            for (Player p : subjects) {
                // Check that the player is still in the world; if not, skip them and don't check them again
                if (!p.getWorld().equals(knife.getWorld())) {
                    subjects.remove(p);
                    continue;
                }

                Location pLoc = p.getLocation();
                Location kLoc = knife.getLocation();

                // Collision detection is done by assuming player is a cylinder and knife is a point
                // Check the Y first - low cost first check, establishes top and bottom face constraints
                double pY = pLoc.getY();
                double kY = kLoc.getY();
                if (kY >= pY && kY <= pY + 2.0) {
                    // Check distance
                    double distX = pLoc.getX() - kLoc.getX();
                    double distZ = pLoc.getZ() - kLoc.getZ();
                    double distXZsquare = distX * distX + distZ * distZ;
                    if (distXZsquare < collisionDistanceSquared) {
                        // Deal 3 hearts of damage, and remove the knife. This no longer needs to execute.
                        p.damage(6.0, knife);
                        knife.remove();
                        cancel();
                        break;
                    }
                }
            }
        }
    }

    private static boolean isAbilityItem(ItemStack item, Material type, String name) {
        if (item != null && item.getType() == type) {
            if (name != null) {
                if (item.hasItemMeta() && name.equals(ChatColor.stripColor(item.getItemMeta().getDisplayName()))) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
