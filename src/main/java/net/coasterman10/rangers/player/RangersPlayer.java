package net.coasterman10.rangers.player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.confuser.barapi.BarAPI;
import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.game.RangersTeam;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class RangersPlayer {
    private static final Map<UUID, RangersPlayer> players = new HashMap<>();
    private static Collection<Material> allowedDrops;
    private static String barMessage;

    public static void initialize(Rangers plugin) {
        barMessage = plugin.getBarMessage();
        Bukkit.getPluginManager().registerEvents(RangersPlayerListener.instance, plugin);
        for (Player player : Bukkit.getOnlinePlayers()) {
            players.put(player.getUniqueId(), new RangersPlayer(player));
        }
    }
    
    public static void setAllowedDrops(Collection<Material> allowedDrops) {
        RangersPlayer.allowedDrops = allowedDrops;
    }

    public static Collection<RangersPlayer> getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    public static RangersPlayer getPlayer(Player player) {
        return players.get(player.getUniqueId());
    }

    public static class RangersPlayerListener implements Listener {
        private static final RangersPlayerListener instance = new RangersPlayerListener();

        // This should be the fist to execute
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerJoin(PlayerJoinEvent e) {
            players.put(e.getPlayer().getUniqueId(), new RangersPlayer(e.getPlayer()));
        }

        // This should be the last to execute
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerLeave(PlayerQuitEvent e) {
            players.remove(e.getPlayer().getUniqueId()).cleanup();
        }
    }

    private final Player bukkitPlayer;
    private Arena arena;
    private RangersTeam team;
    private PlayerType type;
    private PlayerState state = PlayerState.LOBBY;
    private PlayerData data;
    private Location lastSafeLocation;

    private boolean cloaked;

    public RangersPlayer(Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
        data = new PlayerData(this);
    }

    public void cleanup() {
        quit();
        data.save();
    }

    public void joinArena(Arena arena) {
        quit();
        if (arena.addPlayer(this)) {
            this.arena = arena;
        }
    }

    public void quit() {
        if (isInArena()) {
            arena.removePlayer(this);
            arena = null;
        }
        state = PlayerState.LOBBY;
        team = null;
        type = null;
        resetPlayer();
    }

    public void sendMessage(String msg) {
        bukkitPlayer.sendMessage(msg);
    }

    public void teleport(Location lobbySpawn) {
        bukkitPlayer.teleport(lobbySpawn);
    }

    public void resetPlayer() {
        BarAPI.setMessage(bukkitPlayer, barMessage, 100F);
        bukkitPlayer.setGameMode(GameMode.ADVENTURE);
        bukkitPlayer.setAllowFlight(false);
        bukkitPlayer.setHealth(bukkitPlayer.getMaxHealth());
        bukkitPlayer.setFoodLevel(20);
        bukkitPlayer.setSaturation(10F);
        bukkitPlayer.getInventory().clear();
        bukkitPlayer.getInventory().setArmorContents(null);
        bukkitPlayer.setExp(0F);
        bukkitPlayer.setLevel(0);
        bukkitPlayer.setFireTicks(0);
        for (PotionEffect effect : bukkitPlayer.getActivePotionEffects()) {
            bukkitPlayer.removePotionEffect(effect.getType());
        }
        for (Player other : Bukkit.getOnlinePlayers()) {
            other.showPlayer(bukkitPlayer);
        }
    }

    public void addPermanentEffect(PotionEffectType effect, int amp) {
        bukkitPlayer.addPotionEffect(new PotionEffect(effect, Integer.MAX_VALUE, amp, true));
    }

    public void updateSafeLocation() {
        Location loc = bukkitPlayer.getLocation();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        for (int y = loc.getBlockY() + 1; y >= 0; y--) {
            Material type = loc.getWorld().getBlockAt(x, y, z).getType();
            if (type == Material.LAVA || type == Material.STATIONARY_LAVA || type == Material.FIRE)
                break;
            if (type.isSolid()) {
                loc.setX(x + 0.5);
                loc.setY(y + 1.25);
                loc.setZ(z + 0.5);
                if (type == Material.FENCE)
                    loc.add(0.0, 0.5, 0.0);
                lastSafeLocation = loc;
                break;
            }
        }
    }

    public void dropHead() {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(getName());
        String name;
        if (type != null) {
            StringBuilder sb = new StringBuilder(41);
            sb.append(team.getChatColor());
            sb.append("Head of ").append(getName());
            sb.append(" (").append(type.getName()).append(")");
            name = sb.toString();
        } else {
            name = "Head of " + getName();
        }
        meta.setDisplayName(name.toString());
        head.setItemMeta(meta);
        if (lastSafeLocation == null)
            lastSafeLocation = bukkitPlayer.getLocation();
        Item headDrop = lastSafeLocation.getWorld().dropItem(lastSafeLocation, head);
        headDrop.setVelocity(new Vector(0, 0, 0));
        headDrop.teleport(lastSafeLocation);
    }

    public void dropInventory() {
        for (ItemStack item : bukkitPlayer.getInventory()) {
            if (item == null)
                continue;
            Material type = item.getType();
            if (type == Material.SKULL_ITEM || allowedDrops.contains(type)) {
                Item itemDrop = lastSafeLocation.getWorld().dropItem(lastSafeLocation, item);
                itemDrop.setVelocity(new Vector(0, 0, 0));
                itemDrop.teleport(lastSafeLocation);
            }
        }
    }

    public void cloak() {
        if (isCloaked())
            return;
        bukkitPlayer.sendMessage(ChatColor.GOLD + "Cloaked");
        bukkitPlayer.getWorld().playSound(bukkitPlayer.getLocation(), Sound.ENDERMAN_TELEPORT, 0.8F, 1F);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (getPlayer(p).getTeam() != team) {
                p.hidePlayer(bukkitPlayer);
            }
        }
        addPermanentEffect(PotionEffectType.INVISIBILITY, 0);
        cloaked = true;
    }

    public void uncloak() {
        if (!isCloaked())
            return;
        bukkitPlayer.sendMessage(ChatColor.GOLD + "Uncloaked");
        bukkitPlayer.getWorld().playSound(bukkitPlayer.getLocation(), Sound.ENDERMAN_TELEPORT, 0.8F, 1F);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(bukkitPlayer);
        }
        bukkitPlayer.removePotionEffect(PotionEffectType.INVISIBILITY);
        cloaked = false;
    }

    public boolean isCloaked() {
        return cloaked;
    }

    public String getName() {
        return bukkitPlayer.getName();
    }

    public UUID getUniqueId() {
        return bukkitPlayer.getUniqueId();
    }

    public Player getBukkitPlayer() {
        return bukkitPlayer;
    }

    public PlayerData getData() {
        return data;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }

    public Arena getArena() {
        return arena;
    }

    public boolean isInArena() {
        return arena != null;
    }

    public boolean isPlaying() {
        return state == PlayerState.GAME_PLAYING;
    }

    public boolean isSpectating() {
        return state == PlayerState.GAME_SPECTATING;
    }

    public boolean canDoubleJump() {
        return isPlaying() && type == PlayerType.RANGER;
    }

    public RangersTeam getTeam() {
        return team;
    }

    public void setTeam(RangersTeam team) {
        this.team = team;
    }

    public PlayerType getType() {
        return type;
    }

    public void setType(PlayerType type) {
        this.type = type;
    }

    public enum PlayerState {
        LOBBY, GAME_LOBBY, GAME_PLAYING, GAME_SPECTATING;
    }

    public enum PlayerType {
        RANGER, BANDIT, BANDIT_LEADER;

        public String getName() {
            return (name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase()).replace('_', ' ');
        }

        public ChatColor getChatColor() {
            if (this == RANGER) {
                return ChatColor.GREEN;
            } else {
                return ChatColor.RED;
            }
        }
    }
}
