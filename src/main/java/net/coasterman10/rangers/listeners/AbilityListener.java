package net.coasterman10.rangers.listeners;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.coasterman10.rangers.GamePlayer;
import net.coasterman10.rangers.GameTeam;
import net.coasterman10.rangers.PlayerManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class AbilityListener implements Listener {
    private final Plugin plugin;
    private Set<UUID> throwingKnifeCooldowns = new HashSet<>();

    public AbilityListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() != null) {
            if (e.getItem().getType() == Material.TRIPWIRE_HOOK) {
                final Player player = e.getPlayer();
                final UUID id = player.getUniqueId();

                // 5 second cooldown
                if (!throwingKnifeCooldowns.contains(id)) {
                    throwingKnifeCooldowns.add(id);
                    Location eye = player.getEyeLocation();
                    final Entity knife = player.getWorld().dropItem(eye, e.getItem());
                    knife.setVelocity(eye.getDirection().multiply(0.8));

                    // We need to know who shot the knife in case it kills the victim
                    knife.setMetadata("shooter", new FixedMetadataValue(plugin, id));

                    // Hit detection - because I do not feel like using NMS to create my own entity
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // A knife on the ground has certainly missed
                            if (knife.isOnGround()) {
                                knife.remove();
                                cancel();
                                return;
                            }
                            
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                // Skip anyone not in this world
                                if (!p.getWorld().equals(knife.getWorld()))
                                    continue;
                                Location pLoc = player.getLocation();
                                Location kLoc = knife.getLocation();

                                // Check the Y first - low cost first check
                                double pY = pLoc.getY();
                                double kY = kLoc.getY();
                                if (kY >= pY && kY <= pY + 2.0) {
                                    // Check distance is less than 0.2 (distance square is faster)
                                    double distX = pLoc.getX() - kLoc.getX();
                                    double distY = pLoc.getY() - kLoc.getY();
                                    double distXYsquare = distX * distX + distY * distY;
                                    if (distXYsquare < 0.4) {
                                        p.damage(4.0, knife);
                                        knife.remove();
                                        cancel();
                                        return;
                                    }
                                }
                            }
                        }
                    }.runTaskTimer(plugin, 0L, 1L);

                    // Remove the player's ID from the cooldown list after 5 seconds
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            throwingKnifeCooldowns.remove(id);
                        }
                    }.runTaskLater(plugin, 100L);
                }
            }

            if (e.getItem().getType() == Material.SLIME_BALL) {
                Location eye = e.getPlayer().getEyeLocation();
                final Entity striker = eye.getWorld().dropItem(eye, e.getItem());
                striker.setVelocity(eye.getDirection().multiply(0.2));
                
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // TODO: Particle effects and better sound
                        striker.getWorld().playSound(striker.getLocation(), Sound.BLAZE_HIT, 1F, 1.5F);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            // Skip players not in the world
                            if (!player.getWorld().equals(striker.getWorld()))
                                continue;
                            
                            if (player.getLocation().distanceSquared(striker.getLocation()) < 6.25) {
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
    public void onSneak(PlayerToggleSneakEvent e) {
        final GamePlayer player = PlayerManager.getPlayer(e.getPlayer());

        if (player.getGame() == null)
            return; 
        
        if (player.getGame().isRunning() && player.getTeam() == GameTeam.RANGERS
                && player.getUpgradeSelection("ranger.ability").equals("vanish")) {
            if (e.getPlayer().isSneaking()) {
                // If they are still sneaking after 3 seconds, vanish
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.getHandle() != null)
                            if (player.getHandle().isSneaking())
                                player.vanish();
                    }
                }.runTaskLater(plugin, 60L);
            } else {
                if (player.isVanished()) {
                    player.unvanish();
                }
            }
        }
    }
}
