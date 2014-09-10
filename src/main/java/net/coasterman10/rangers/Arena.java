package net.coasterman10.rangers;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

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
        return map.lobbySpawn.addTo(lobby);
    }

    public Location getRangerSpawn() {
        return map.rangerSpawn.addTo(game);
    }

    public Location getBanditSpawn() {
        return map.banditSpawn.addTo(game);
    }

    public Location getRangerChest() {
        return game.clone().add(map.rangerChest);
    }

    public Location getBanditChest() {
        return game.clone().add(map.banditChest);
    }

    public void clearGround() {
        Vector min = game.toVector().setY(0);
        Vector max = min.clone()
                .add(new Vector(map.gameSchematic.getWidth(), game.getWorld().getMaxHeight(), map.gameSchematic
                        .getLength()));
        for (Entity e : game.getWorld().getEntitiesByClasses(Item.class, Arrow.class)) {
            if (e.getLocation().toVector().isInAABB(min, max)) {
                e.remove();
            }
        }
    }

    public Location getSpectatorSpawn() {
        return map.spectatorSpawn.addTo(game);
    }
}
