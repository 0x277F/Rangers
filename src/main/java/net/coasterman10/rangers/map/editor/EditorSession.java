package net.coasterman10.rangers.map.editor;

import java.io.File;

import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.SpawnVector;
import net.coasterman10.rangers.game.GameTeam;
import net.coasterman10.rangers.map.GameMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;

public class EditorSession {
    private final Rangers plugin;
    private final Player player;
    private final GameMap map;
    private final Location origin;

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
