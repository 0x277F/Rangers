package net.coasterman10.rangers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.coasterman10.rangers.listeners.PlayerListener;
import net.coasterman10.rangers.listeners.WorldListener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Rangers extends JavaPlugin {
    // Guess what? I have the print margin at 120 characters. If you have a tiny screen or use a terminal for Java for
    // some insane reason, please make a pull request to reduce the margin to the 80 character mark.
    // It will be denied anyways but at least I'll know how many people care about it that much.
    // Without any further adieu, let us dive into the horrid mess that is code by coasterman10.

    private Map<Integer, Game> games = new HashMap<>();
    
    private WorldListener worldListener = new WorldListener();
    private PlayerListener playerListener = new PlayerListener();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveDefaultConfigValues();
        loadConfig();
        
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(worldListener, this);
        pm.registerEvents(playerListener, this);
    }

    @Override
    public void onDisable() {

    }

    private void saveDefaultConfigValues() {
        // Iterate through all the defaults and write their values to the config if nothing is set.
        // Purely idiot-proofing.
        for (Entry<String, Object> value : getConfig().getDefaults().getValues(true).entrySet())
            if (!getConfig().isSet(value.getKey()))
                getConfig().set(value.getKey(), value.getValue());
        saveConfig();
    }

    private void loadConfig() {
        // Load the lobby spawn location. Default is in world "lobby" at location (0,64,0). If the world doesn't exist,
        // create it to save ourselves the hassle of setting the thing up.
        String lobbyWorldName = getConfig().getString("lobby.world");
        World lobbyWorld = new WorldCreator(lobbyWorldName).generator(new EmptyChunkGenerator()).createWorld();
        double lobbyX = getConfig().getDouble("lobby.x");
        double lobbyY = getConfig().getDouble("lobby.y");
        double lobbyZ = getConfig().getDouble("lobby.z");
        playerListener.setLobbyLocation(new Location(lobbyWorld, lobbyX, lobbyY, lobbyZ));

        // Iterate over the list of maps in the config file with the sign locations
        Map<Location, GameSign> signs = new HashMap<>();
        List<Map<?, ?>> mapList = getConfig().getMapList("signs");
        for (Map<?, ?> map : mapList) {
            // Objects since we don't know what type these are yet
            Object xx = map.get("x");
            Object yy = map.get("y");
            Object zz = map.get("z");

            // Check that all the objects are numbers; if not, just cancel
            if (xx instanceof Number && yy instanceof Number && zz instanceof Number) {
                int x = ((Number) xx).intValue();
                int y = ((Number) yy).intValue();
                int z = ((Number) zz).intValue();

                Location loc = new Location(lobbyWorld, x, y, z);
                Block b = loc.getBlock();
                if (!(b.getState() instanceof Sign))
                    placeSign(loc); // Build a new sign at the location (idiot-proofing)
                GameSign sign = new GameSign(b);
                signs.put(loc, sign);
                
                // Each sign corresponds to a game, which we initialize here
                Game g = new Game(sign);
                games.put(g.getId(), g);
                sign.setGame(g);
            }
        }
        playerListener.setSigns(signs);
        
        // Load the allowed drops list
        Collection<Material> allowedDrops = new HashSet<>();
        List<Integer> allowedDropIds = getConfig().getIntegerList("allowed-drops");
        for (Integer i : allowedDropIds) {
            @SuppressWarnings("deprecation")
            Material m = Material.getMaterial(i);
            allowedDrops.add(m);
        }
        playerListener.setAllowedDrops(allowedDrops);
    }

    private static void placeSign(Location loc) {
        if (loc.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
            loc.getBlock().setType(Material.SIGN); // There is a solid block below, we can just place a sign there
        } else {
            // Check every block face for a solid block; if we find a solid block, place a wall sign
            for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST }) {
                if (loc.getBlock().getRelative(face).getType().isSolid()) {
                    BlockFace facing = face.getOppositeFace();
                    BlockState state = loc.getBlock().getState();
                    state.setType(Material.WALL_SIGN);
                    org.bukkit.material.Sign data = new org.bukkit.material.Sign(state.getType());
                    data.setFacingDirection(facing);
                    state.update();
                    break;
                }
            }
        }
    }
}
