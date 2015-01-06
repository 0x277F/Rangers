package net.coasterman10.rangers.listeners;

import java.util.UUID;

import net.coasterman10.rangers.arena.ClassicArena;
import net.coasterman10.rangers.player.RangersPlayer;
import net.coasterman10.rangers.player.RangersPlayer.PlayerState;
import net.minecraft.server.v1_7_R3.EnumClientCommand;
import net.minecraft.server.v1_7_R3.PacketPlayInClientCommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDeathListener implements Listener {
    private final Plugin plugin;

    public PlayerDeathListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        RangersPlayer player = RangersPlayer.getPlayer(e.getPlayer());
        if (player.isPlaying() && player.getArena() instanceof ClassicArena) {
            player.dropHead();
            player.dropInventory();
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        new RespawnTask(e.getEntity().getUniqueId()).runTaskLater(plugin, 1L);

        e.getDrops().clear();
        e.setDeathMessage(null);
        
        RangersPlayer player = RangersPlayer.getPlayer(e.getEntity());
        if (!player.isPlaying()) {
            return;
        }

        if (player.isPlaying() && player.getArena() instanceof ClassicArena) {
            player.setState(PlayerState.GAME_LOBBY);
            player.dropHead();
            player.dropInventory();
        }

        // Huge mess of code to generate death message. Don't ask.
        StringBuilder msg = new StringBuilder(128);

        // Victim's name
        msg.append(player.getType().getChatColor());
        msg.append(player.getName());
        msg.append("(").append(player.getType().getName()).append(")");

        EntityDamageEvent cause = e.getEntity().getLastDamageCause();
        if (cause instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) cause).getDamager();

            if (damager instanceof Player) { // Victim killed by another player
                msg.append(ChatColor.DARK_RED).append(" was slain by ");
                RangersPlayer killer = RangersPlayer.getPlayer((Player) damager);

                // Killer's name
                msg.append(killer.getType().getChatColor());
                msg.append(killer.getName());
                msg.append("(").append(killer.getType().getName()).append(")");

                // Item used by killer
                ItemStack item = ((Player) damager).getItemInHand();
                if (item != null) {
                    msg.append(ChatColor.DARK_RED).append(" using a ");

                    // Try to use item's display name
                    String itemName = null;
                    if (item.hasItemMeta()) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta.hasDisplayName()) {
                            itemName = meta.getDisplayName();
                        }
                    }

                    // Use material name if no display name is set
                    if (itemName == null) {
                        itemName = item.getType().name().toLowerCase().replace('_', ' ');
                    }

                    msg.append(ChatColor.YELLOW).append(itemName);
                } else {
                    // Killer used their bare hands
                    msg.append(ChatColor.DARK_RED).append(" using their ");
                    msg.append(ChatColor.YELLOW).append("BARE HANDS!");
                }
            } else if (damager instanceof Arrow) { // Victim shot with an arrow
                ProjectileSource shooter = ((Arrow) damager).getShooter();

                if (shooter instanceof Player) { // Victim shot by a player
                    msg.append(ChatColor.DARK_RED).append(" was shot by ");

                    // Killer's name
                    RangersPlayer killer = RangersPlayer.getPlayer((Player) shooter);
                    msg.append(killer.getType().getChatColor());
                    msg.append(killer.getName());
                    msg.append("(").append(killer.getType().getName()).append(")");

                    // Search for the killer's bow
                    ItemStack bow = null;
                    for (ItemStack item : ((Player) shooter).getInventory()) {
                        if (item == null)
                            continue;
                        if (item.getType() == Material.BOW) {
                            bow = item;
                            break;
                        }
                    }
                    if (bow != null) {
                        msg.append(ChatColor.DARK_RED).append(" using a ");

                        // Try to use bow's display name
                        String itemName = null;
                        if (bow.hasItemMeta()) {
                            ItemMeta meta = bow.getItemMeta();
                            if (meta.hasDisplayName()) {
                                itemName = meta.getDisplayName();
                            }
                        }

                        // Use material name if no display name is set
                        if (itemName == null) {
                            itemName = "bow";
                        }

                        msg.append(ChatColor.YELLOW).append(itemName);
                    } else {
                        // Killer used their bare hands
                        msg.append(ChatColor.DARK_RED).append(" using their ");
                        msg.append(ChatColor.YELLOW).append("BARE HANDS!");
                    }
                } else { // Victim was shot by some other entity
                    msg.append(ChatColor.DARK_RED).append(" was shot");
                }
            } else if (damager instanceof Item) { // Victim killed by an item drop
                ItemStack item = ((Item) damager).getItemStack();
                if (item.getType() == Material.TRIPWIRE_HOOK) {
                    msg.append(ChatColor.DARK_RED).append(" was killed by ");
                    for (MetadataValue value : damager.getMetadata("shooter")) {
                        if (value.getOwningPlugin().equals(plugin)) {
                            // The metadata value is the already prepared text we need for the death message
                            msg.append(value.asString());
                            msg.append(ChatColor.DARK_RED).append(" using a ");
                            msg.append(ChatColor.YELLOW).append("throwing knife");
                            break;
                        }
                    }

                    // It appears our throwing knife was thrown by no one! This should never happen
                    msg.append("a paranormally spawned ");
                    msg.append(ChatColor.YELLOW).append("throwing knife");
                }
            } else { // Victim killed by some other entity
                msg.append(ChatColor.DARK_RED).append(" was killed");
            }
        } else if (cause.getCause() == DamageCause.FALL) {
            msg.append(ChatColor.DARK_RED).append(" hit the ground too hard");
        } else if (cause.getCause() == DamageCause.LAVA) {
            msg.append(ChatColor.DARK_RED).append(" took a bath in lava");
        } else {
            // TODO: Fill in alternate death reasons
            msg.append(ChatColor.DARK_RED).append(" died");
        }

        player.getArena().broadcast(msg.toString());
    }
    
    public class RespawnTask extends BukkitRunnable {
        private final UUID id;

        public RespawnTask(UUID id) {
            this.id = id;
        }

        @Override
        public void run() {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                ((CraftPlayer) player).getHandle().playerConnection.a(new PacketPlayInClientCommand(
                        EnumClientCommand.PERFORM_RESPAWN));
            }
        }
    }
}
