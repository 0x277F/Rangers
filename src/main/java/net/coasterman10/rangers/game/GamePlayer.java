package net.coasterman10.rangers.game;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import net.coasterman10.rangers.Rangers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class GamePlayer {
    private static final int DOUBLE_JUMP_PERIOD = 5;
    private static final int DOUBLE_JUMP_TICKS = 160;

    public final UUID id;
    private Game game;
    private GameTeam team;
    private boolean doubleJump;
    private boolean alive;
    private boolean cloaked;

    private static Collection<String> upgradeCategories = new HashSet<>();

    static {
        upgradeCategories.add("ranger.ability");
        upgradeCategories.add("ranger.secondary");
        upgradeCategories.add("ranger.bow");
        upgradeCategories.add("bandit.ability");
        upgradeCategories.add("bandit.secondary");
        upgradeCategories.add("bandit.bow");
    }

    // Upgrades the player can get:
    // ranger.ability - none, cloak
    // ranger.bow - none, 16arrows
    // ranger.secondary - throwingknife, strikers
    // bandit.secondary - bow, mace
    // bandit.bow - none, 8arrows
    // bandit.ability - none, grapple
    private HashMap<String, String> upgrades = new HashMap<>();

    public GamePlayer(UUID id) {
        this.id = id;
        upgrades.put("ranger.ability", "none");
        upgrades.put("ranger.secondary", "throwingknife");
        upgrades.put("ranger.bow", "none");
        upgrades.put("bandit.ability", "none");
        upgrades.put("bandit.secondary", "bow");
        upgrades.put("bandit.bow", "none");
    }

    public void loadData() {
        File dataFile = new File(new File(Rangers.instance().getDataFolder(), "players"), id.toString() + ".yml");
        if (dataFile.exists()) {
            Configuration config = YamlConfiguration.loadConfiguration(dataFile);
            for (String upgrade : upgradeCategories) {
                String value = config.getString("upgrades." + upgrade, null);
                if (value != null) {
                    upgrades.put(upgrade, value);
                }
            }
        }
    }

    public void saveData() {
        FileConfiguration config = new YamlConfiguration();
        for (String upgrade : upgradeCategories) {
            config.set("ugrades." + upgrade, upgrades.get(upgrade));
        }
        try {
            config.save(new File(new File(Rangers.instance().getDataFolder(), "players"), id.toString() + ".yml"));
        } catch (IOException e) {
            Rangers.instance().getLogger()
                    .warning("Could not save player data for " + getName() + " (" + id.toString() + ")");
        }
    }

    public void quit() {
        // Perform any cleanup when the player leaves
        if (isInGame())
            game.removePlayer(this);
        alive = false;
        cloaked = false;
        setCanDoubleJump(false);
    }

    public Player getHandle() {
        return Bukkit.getPlayer(id);
    }

    public String getName() {
        return getHandle().getName();
    }

    public void setTeam(GameTeam team) {
        this.team = team;
    }

    public GameTeam getTeam() {
        return team;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public boolean isInGame() {
        return game != null;
    }

    public boolean isBanditLeader() {
        return isInGame() && game instanceof ClassicGame && this == ((ClassicGame) game).getBanditLeader();
    }

    public String getUpgradeSelection(String name) {
        String s = upgrades.get(name);
        return s != null ? s : "default";
    }

    public void setUpgradeSelection(String name, String value) {
        upgrades.put(name, value);
    }

    public boolean isCloaked() {
        return cloaked;
    }

    public void cloak() {
        if (isCloaked())
            return;
        getHandle().sendMessage(ChatColor.GOLD + "Cloaked");
        getHandle().getWorld().playSound(getHandle().getLocation(), Sound.ENDERMAN_TELEPORT, 0.8F, 1F);
        for (Player p : Bukkit.getOnlinePlayers())
            p.hidePlayer(getHandle());
        cloaked = true;
    }

    public void uncloak() {
        if (!isCloaked())
            return;
        getHandle().sendMessage(ChatColor.GOLD + "Uncloaked");
        getHandle().getWorld().playSound(getHandle().getLocation(), Sound.ENDERMAN_TELEPORT, 0.8F, 1F);
        for (Player p : Bukkit.getOnlinePlayers())
            p.showPlayer(getHandle());
        cloaked = false;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setCanDoubleJump(boolean doubleJump) {
        this.doubleJump = doubleJump;
        if (doubleJump) {
            enableDoubleJump();
        } else {
            Player p = getHandle();
            if (p != null) {
                p.setLevel(0);
                p.setExp(0);
                p.setFlying(false);
                p.setAllowFlight(false);
            }
        }
    }

    public void doubleJump() {
        Player p = getHandle();
        if (p == null)
            return;
        p.setFlying(false);
        p.setAllowFlight(false);
        p.setExp(0);

        p.setVelocity(p.getLocation().getDirection().multiply(0.5).add(new Vector(0.0, 1.25, 0.0)));
        p.getWorld().playEffect(p.getLocation().add(0.0, 0.5, 0.0), Effect.SMOKE, 4);
        p.getWorld().playSound(p.getLocation(), Sound.ZOMBIE_INFECT, 1.0F, 2.0F);

        new BukkitRunnable() {
            int time = DOUBLE_JUMP_TICKS;

            @Override
            public void run() {
                // Immediately cancel if double jump has been disabled
                if (!doubleJump) {
                    cancel();
                    return;
                }

                Player p = getHandle();
                if (p != null) {
                    if (time == 0) {
                        enableDoubleJump();
                        cancel();
                    } else {
                        // Animate the bar refilling
                        p.setExp((float) (DOUBLE_JUMP_TICKS - time) / (float) DOUBLE_JUMP_TICKS);
                        time -= DOUBLE_JUMP_PERIOD;
                    }
                } else {
                    // Not much point in this if they are offline
                    cancel();
                }
            }
        }.runTaskTimer(Rangers.instance(), 0L, (long) DOUBLE_JUMP_PERIOD);
    }

    public boolean canDoubleJump() {
        if (getHandle() == null)
            return false;
        return doubleJump && getHandle().getExp() == Float.intBitsToFloat(Float.floatToIntBits(1F) - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GamePlayer))
            return false;
        return ((GamePlayer) o).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private void enableDoubleJump() {
        Player p = getHandle();
        if (p == null)
            return;
        // Maximum value of float minus one to make bar appear full
        p.setExp(doubleJump ? Float.intBitsToFloat(Float.floatToIntBits(1F) - 1) : 0);
        p.setAllowFlight(true);
        p.setFlying(false); // Prevents them from being in the flying state on accident
        p.playSound(p.getEyeLocation(), Sound.WITHER_SHOOT, 0.75F, 1.0F);
        p.sendMessage(ChatColor.GREEN + "Double Jump ability recharged");
    }

    public void dropHead(Location loc) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(getName());
        StringBuilder name = new StringBuilder(32);
        if (team != null)
            name.append(team.getChatColor());
        name.append("Head of ");
        name.append(getName());
        if (team != null)
            if (isBanditLeader())
                name.append(" (Bandit Leader)");
            else
                name.append(" (").append(team.getName()).append(")");
        meta.setDisplayName(name.toString());
        head.setItemMeta(meta);
        Item i = loc.getWorld().dropItem(loc, head);
        i.setVelocity(new Vector(0, 0, 0));
        i.teleport(loc);
    }
}
