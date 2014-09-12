package net.coasterman10.rangers.map.editor;

import java.util.UUID;

import net.coasterman10.rangers.SpawnVector;
import net.coasterman10.rangers.map.GameMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

public class EditorSession {
    private final UUID id;
    private final GameMap map;
    private final Location origin;

    public EditorSession(Player player, GameMap map, Location origin) {
        id = player.getUniqueId();
        this.map = map;
        this.origin = origin;
    }

    public Player getHandle() {
        return Bukkit.getPlayer(id);
    }

    public GameMap getMap() {
        return map;
    }

    public Location getOrigin() {
        return origin;
    }

    public SpawnVector getVectorizedLocation() {
        return new SpawnVector(getHandle().getLocation()).subtract(origin.toVector());
    }

    @SuppressWarnings("deprecation")
    public BlockVector getTargetBlock() {
        return getHandle().getTargetBlock(null, 100).getLocation().toVector().subtract(origin.toVector())
                .toBlockVector();
    }
}
