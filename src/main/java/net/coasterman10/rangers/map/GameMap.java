package net.coasterman10.rangers.map;

import java.util.EnumMap;
import java.util.Map;

import net.coasterman10.rangers.SpawnVector;
import net.coasterman10.rangers.game.GameTeam;
import net.coasterman10.schematicutil.Schematic;

import org.bukkit.util.BlockVector;

public class GameMap {
    public final String name;
    private Schematic schematic;
    private String schematicFilename;
    private SpawnVector lobbySpawn;
    private SpawnVector spectatorSpawn;
    private Map<GameTeam, SpawnVector> spawns = new EnumMap<>(GameTeam.class);
    private Map<GameTeam, BlockVector> chests = new EnumMap<>(GameTeam.class);

    public GameMap(String name) {
        this.name = name;
    }

    public Schematic getSchematic() {
        return schematic;
    }
    
    public String getSchematicFilename() {
        return schematicFilename;
    }

    public SpawnVector getLobbySpawn() {
        return lobbySpawn;
    }
    
    public SpawnVector getSpectatorSpawn() {
        return spectatorSpawn;
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
    
    public void setSchematicFilename(String schematicFilename) {
        this.schematicFilename = schematicFilename;
    }

    public void setLobbySpawn(SpawnVector lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }
    
    public void setSpectatorSpawn(SpawnVector spectatorSpawn) {
        this.spectatorSpawn = spectatorSpawn;
    }

    public void setSpawn(GameTeam team, SpawnVector spawn) {
        spawns.put(team, spawn);
    }

    public void setChest(GameTeam team, BlockVector chest) {
        chests.put(team, chest);
    }

    public boolean isValid() {
        if (schematic == null || lobbySpawn == null || spectatorSpawn == null)
            return false;
        for (GameTeam team : GameTeam.values()) {
            if (!spawns.containsKey(team))
                return false;
            if (!chests.containsKey(team))
                return false;
        }
        return true;
    }
}
