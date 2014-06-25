package net.coasterman10.rangers;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class GameMap {
    public final String name;
    public Schematic lobbySchematic = new Schematic();
    public Schematic gameSchematic = new Schematic();
    public Vector lobbySpawn = new Vector();
    public Vector rangerSpawn = new Vector();
    public Vector banditSpawn = new Vector();
    public BlockVector rangerHopper = new BlockVector();
    public BlockVector banditHopper = new BlockVector();
    
    public GameMap(String name) {
        this.name = name;
    }
}
