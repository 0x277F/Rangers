package net.coasterman10.rangers;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class Arena {
    private GameMap map;
    private Location lobby;
    private Location game;
    private boolean used;

    public Arena(GameMap map, Location lobby, Location game) {
        this.map = map;
        this.lobby = lobby;
        this.game = game;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isUsed() {
        return used;
    }

    public String getMapName() {
        return map.name;
    }
    
    public void build(Plugin plugin) {
        map.lobbySchematic.buildDelayed(lobby, plugin);
        map.gameSchematic.buildDelayed(game, plugin);
    }
    
    public Location getOrigin() {
        return lobby;
    }

    public Location getLobbySpawn() {
        return lobby.clone().add(map.lobbySpawn);
    }

    public Location getRangerSpawn() {
        return game.clone().add(map.rangerSpawn);
    }

    public Location getBanditSpawn() {
        return game.clone().add(map.banditSpawn);
    }

    public Location getRangerChest() {
        return game.clone().add(map.rangerChest);
    }

    public Location getBanditChest() {
        return game.clone().add(map.banditChest);
    }
}
