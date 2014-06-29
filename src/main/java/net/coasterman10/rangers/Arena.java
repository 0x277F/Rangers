package net.coasterman10.rangers;

import org.bukkit.Location;

public class Arena {
    private String mapName;
    private Location lobby;
    private Location arena;
    private boolean used;
    
    public Arena(String mapName, Location lobby, Location arena) {
        this.mapName = mapName;
        this.lobby = lobby;
        this.arena = arena;
    }
    
    public void setUsed(boolean used) {
        this.used = used;
    }
    
    public boolean isUsed() {
        return used;
    }
    
    public String getMapName() {
        return mapName;
    }
    
    public Location getLobbyLocation() {
        return lobby;
    }
    
    public Location getArenaLocation() {
        return arena;
    }
}
