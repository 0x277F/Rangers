package net.coasterman10.rangers;

import org.bukkit.util.BlockVector;

public class GameMap {
    public final String name;
    public Schematic lobbySchematic = new Schematic();
    public Schematic gameSchematic = new Schematic();
    public SpawnVector lobbySpawn = new SpawnVector();
    public SpawnVector rangerSpawn = new SpawnVector();
    public SpawnVector banditSpawn = new SpawnVector();
    public SpawnVector spectatorSpawn = new SpawnVector();
    public BlockVector rangerChest = new BlockVector();
    public BlockVector banditChest = new BlockVector();
    
    public GameMap(String name) {
        this.name = name;
    }
}
