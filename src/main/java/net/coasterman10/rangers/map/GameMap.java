package net.coasterman10.rangers.map;

import java.util.EnumMap;
import java.util.Map;

import net.coasterman10.rangers.SpawnVector;
import net.coasterman10.rangers.game.GameTeam;

import org.bukkit.util.BlockVector;

public class GameMap {
    public final String name;
    private Schematic schematic;
    private SpawnVector lobbySpawn;
    private Map<GameTeam, SpawnVector> spawns = new EnumMap<>(GameTeam.class);
    private Map<GameTeam, BlockVector> chests = new EnumMap<>(GameTeam.class);

    public GameMap(String name) {
        this.name = name;
    }

    public Schematic getSchematic() {
        return schematic;
    }

    public SpawnVector getLobbySpawn() {
        return lobbySpawn;
    }

    public SpawnVector getSpawn(GameTeam team) {
        return spawns.get(team);
    }

    public BlockVector getChest(GameTeam team) {
        return chests.get(team);
    }

    public void setSchematic(Schematic schematic) {
        this.schematic = schematic;
    }

    public void setLobbySpawn(SpawnVector lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    public void setSpawn(GameTeam team, SpawnVector spawn) {
        spawns.put(team, spawn);
    }

    public void setChest(GameTeam team, BlockVector chest) {
        chests.put(team, chest);
    }

    public boolean isValid() {
        if (schematic == null || lobbySpawn == null)
            return false;
        for (GameTeam team : GameTeam.values()) {
            if (!spawns.containsKey(team))
                return false;
            if (!chests.containsKey(team) && team != GameTeam.SPECTATORS)
                return false;
        }
        return true;
    }
}
