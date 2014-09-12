package net.coasterman10.rangers.map.editor;

import java.io.File;

import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.SpawnVector;
import net.coasterman10.rangers.game.GameTeam;
import net.coasterman10.rangers.map.GameMap;
import net.coasterman10.rangers.map.Schematic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class EditorSession {
    private final Rangers plugin;
    private final Player player;
    private final GameMap map;
    private Location origin;

    public EditorSession(Rangers plugin, Player player, GameMap map, Location origin) {
        this.plugin = plugin;
        this.player = player;
        this.map = map;
        this.origin = origin;
    }

    public void load() {
        player.sendMessage(ChatColor.GREEN + "Opening map \"" + map.name + "\"...");
        map.getSchematic().buildDelayed(origin, plugin);
        player.teleport(map.getSpawn(GameTeam.SPECTATORS).addTo(origin));
    }
    
    public void unload() {
        player.sendMessage(ChatColor.GREEN + "Closing editor for map \"" + map.name + "\"");
        player.teleport(plugin.getLobbySpawn());
        Bukkit.unloadWorld(origin.getWorld(), false);
        new DeleteWorldTask(origin.getWorld().getName()).runTaskAsynchronously(plugin);
    }
    
    public void setLobbySpawn() {
        map.setLobbySpawn(getVectorizedLocation());
    }
    
    public void setSpawn(GameTeam team) {
        map.setSpawn(team, getVectorizedLocation());
    }

    public GameMap getMap() {
        return map;
    }

    public Location getOrigin() {
        return origin;
    }

    public SpawnVector getVectorizedLocation() {
        return new SpawnVector(player.getLocation()).subtract(origin.toVector());
    }

    @SuppressWarnings("deprecation")
    public BlockVector getTargetBlock() {
        return player.getTargetBlock(null, 100).getLocation().toVector().subtract(origin.toVector()).toBlockVector();
    }
    
    private void updateBounds() {
        origin = findMinimum().toLocation(origin.getWorld());
        Location max = findMaximum().toLocation(origin.getWorld());
        map.setSchematic(new Schematic(origin, max));
    }
    
    private Vector findMinimum() {
        Vector min = origin.toVector();
        for (Chunk c : origin.getWorld().getLoadedChunks()) {
            if (c.getX() * 16 > min.getX() || c.getZ() * 16 > min.getZ())
                continue;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < min.getY(); y++) {
                    for (int z = 0; z < 16; z++) {
                        if (c.getBlock(x, y, z).getType() != Material.AIR) {
                            min.setX(Math.min(c.getX() * 16 + x, min.getBlockX()));
                            min.setY(Math.min(y, min.getBlockY()));
                            min.setZ(Math.min(c.getZ() * 16 + z, min.getBlockZ()));
                        }
                    }
                }
            }
        }
        return min;
    }
    
    private Vector findMaximum() {
        Vector max = origin.toVector();
        for (Chunk c : origin.getWorld().getLoadedChunks()) {
            if (c.getX() * 16 < max.getX() || c.getZ() * 16 < max.getZ())
                continue;
            for (int x = 0; x < 16; x++) {
                for (int y = max.getBlockX(); y < origin.getWorld().getMaxHeight(); y++) {
                    for (int z = 0; z < 16; z++) {
                        if (c.getBlock(x, y, z).getType() != Material.AIR) {
                            max.setX(Math.max(c.getX() * 16 + x, max.getBlockX()));
                            max.setY(Math.max(y, max.getBlockY()));
                            max.setZ(Math.max(c.getZ() * 16 + z, max.getBlockZ()));
                        }
                    }
                }
            }
        }
        return max;
    }
    
    private static class DeleteWorldTask extends BukkitRunnable {
        private final String worldName;
        
        public DeleteWorldTask(String worldName) {
            this.worldName = worldName;
        }
        
        @Override
        public void run() {
            recursiveDelete(new File(Bukkit.getWorldContainer(), worldName));
        }

        private static void recursiveDelete(File file) {
            if (file.isDirectory())
                for (File f : file.listFiles())
                    recursiveDelete(f);
            else
                file.delete();
        }
    }
}
