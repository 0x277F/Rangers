package net.coasterman10.rangers;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GamePlayer {
    private final UUID id;
    private Game game;
    private GameTeam team;
    private boolean alive;
    private boolean banditLeader;
    
    private HashMap<String, String> upgrades;
    
    public GamePlayer(UUID id) {
        this.id = id;
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
    
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public void setBanditLeader(boolean banditLeader) {
        this.banditLeader = banditLeader;
    }
    
    public boolean isBanditLeader() {
        return banditLeader;
    }
    
    public String getUpgradeSelection(String name) {
        return upgrades.get(name);
    }
}
