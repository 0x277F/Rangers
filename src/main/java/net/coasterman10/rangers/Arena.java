package net.coasterman10.rangers;

import org.bukkit.Location;

public class Arena {
    private GameMap map;
    private Location lobby;
    private Location arena;
    private boolean used;
    
    public Arena(GameMap map, Location lobby, Location arena) {
        this.map = map;
        this.lobby = lobby;
        this.arena = arena;
    }
    
    public void setUsed(boolean used) {
        this.used = used;
    }
    
    public boolean isUsed() {
        return used;
    }
    
    public GameMap getMap() {
        return map;
    }
    
    public Location getLobbyLocation() {
        return lobby;
    }
    
    public Location getArenaLocation() {
        return arena;
    }
}
