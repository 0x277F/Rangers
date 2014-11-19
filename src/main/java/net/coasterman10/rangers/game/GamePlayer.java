package net.coasterman10.rangers.game;

import java.util.HashMap;
import java.util.UUID;

import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.util.TaskCollection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class GamePlayer {
    private static final int VANISH_TIME = 45;
    private static final int VANISH_COOLDOWN = 60;

    private static final int DOUBLE_JUMP_PERIOD = 5;
    private static final int DOUBLE_JUMP_TICKS = 160;

    public final UUID id;
    private Game game;
    private GameTeam team;
    private boolean doubleJump;
    private boolean ingame;

    private boolean vanished;
    private long lastVanish;
    private TaskCollection vanishTasks = new TaskCollection();

    // Upgrades the player can get:
    // ranger.ability - none, vanish
    // ranger.bow - none, 16arrows
    // ranger.secondary - throwingknife, strikers
    // bandit.secondary - bow, mace
    // bandit.bow - none, 8arrows
    private HashMap<String, String> upgrades = new HashMap<>();

    public GamePlayer(UUID id) {
        this.id = id;
        upgrades.put("ranger.ability", "none");
        upgrades.put("ranger.bow", "none");
        upgrades.put("ranger.secondary", "throwingknife");
        upgrades.put("bandit.secondary", "bow");
        upgrades.put("bandit.bow", "none");
    }
    
    public void quit() {
        // Perform any cleanup when the player leaves
        vanishTasks.cancelAll();
        if (isInGame())
            game.removePlayer(this);
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
        return isInGame() && equals(game.getBanditLeader());
    }

    public String getUpgradeSelection(String name) {
        String s = upgrades.get(name);
        return s != null ? s : "default";
    }

    public void setUpgradeSelection(String name, String value) {
        upgrades.put(name, value);
    }

    public boolean isVanished() {
        return vanished;
    }

    public void vanish() {
        if (System.currentTimeMillis() - lastVanish < 1000 * VANISH_COOLDOWN)
            getHandle().sendMessage(ChatColor.RED + "I can't hide again so quickly!");

        getHandle().sendMessage(ChatColor.DARK_AQUA + "Vanished");
        getHandle().getWorld().playSound(getHandle().getLocation(), Sound.ENDERMAN_TELEPORT, 0.8F, 1F);
        for (Player p : Bukkit.getOnlinePlayers())
            p.hidePlayer(getHandle());
        vanished = true;

        // Send some warning messages and cut the player off at 45 seconds
        vanishTasks.addTask(new BukkitRunnable() {
            @Override
            public void run() {
                getHandle().sendMessage(ChatColor.GOLD + "I don't think I can hide for much longer...");
            }
        }.runTaskLater(Rangers.instance(), (VANISH_TIME - 15) * 20L));
        vanishTasks.addTask(new BukkitRunnable() {
            @Override
            public void run() {
                getHandle().sendMessage(ChatColor.RED + "I can only hide for a few more seconds!");
            }
        }.runTaskLater(Rangers.instance(), (VANISH_TIME - 5) * 20L));
        vanishTasks.addTask(new BukkitRunnable() {
            @Override
            public void run() {
                unvanish();
            }
        }.runTaskLater(Rangers.instance(), VANISH_TIME * 20L));
    }

    public void unvanish() {
        getHandle().sendMessage(ChatColor.DARK_AQUA + "Unvanished");
        getHandle().getWorld().playSound(getHandle().getLocation(), Sound.ENDERMAN_TELEPORT, 0.8F, 1F);
        for (Player p : Bukkit.getOnlinePlayers())
            p.showPlayer(getHandle());
        vanished = false;
        lastVanish = System.currentTimeMillis();
        vanishTasks.cancelAll();
    }

    public void setAlive(boolean alive) {
        this.ingame = alive;
    }

    public boolean isAlive() {
        return ingame;
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

        p.setVelocity(p.getLocation().getDirection().multiply(1.3).add(new Vector(0.0, 1, 0.0)));
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
}
