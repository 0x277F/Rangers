package net.coasterman10.rangers.game;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class GamePlayer {
    public final UUID id;
    private Game game;
    private GameTeam team;
    private boolean banditLeader;
    private boolean vanished;

    // Upgrades the player can get:
    // ranger.ability - none, vanish
    // ranger.bow - none, 16arrows
    // ranger.secondary - throwingknife, strikers
    // bandit.secondary - bow, mace
    private HashMap<String, String> upgrades = new HashMap<>();

    public GamePlayer(UUID id) {
        this.id = id;
        upgrades.put("ranger.ability", "none");
        upgrades.put("ranger.bow", "none");
        upgrades.put("ranger.secondary", "throwingknife");
        upgrades.put("bandit.secondary", "bow");
    }

    public Player getHandle() {
        return Bukkit.getPlayer(id);
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

    public void setBanditLeader(boolean banditLeader) {
        this.banditLeader = banditLeader;
    }

    public boolean isBanditLeader() {
        return banditLeader;
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
        getHandle().sendMessage(ChatColor.RED + "Vanished");
        getHandle().getWorld().playSound(getHandle().getLocation(), Sound.ENDERMAN_TELEPORT, 0.8F, 1F);
        for (Player p : Bukkit.getOnlinePlayers())
            p.hidePlayer(getHandle());
        vanished = true;
    }

    public void unvanish() {
        getHandle().sendMessage(ChatColor.RED + "UnVanished");
        getHandle().getWorld().playSound(getHandle().getLocation(), Sound.ENDERMAN_TELEPORT, 0.8F, 1F);
        for (Player p : Bukkit.getOnlinePlayers())
            p.showPlayer(getHandle());
        vanished = false;
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
}
