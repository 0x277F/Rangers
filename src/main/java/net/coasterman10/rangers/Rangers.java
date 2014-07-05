package net.coasterman10.rangers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import net.coasterman10.rangers.config.ConfigAccessor;
import net.coasterman10.rangers.config.PluginConfigAccessor;
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
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Rangers extends JavaPlugin {
    private static Logger log;

    public static Logger logger() {
        return log;
    }

    private Location lobbySpawn;
    private World gameWorld;
    private Map<Location, GameSign> signs = new HashMap<>();
    private Map<UUID, PlayerData> players = new HashMap<>();

    private GameMapManager gameMapManager;
    private WorldListener worldListener;
    private PlayerListener playerListener;
    private ArenaManager arenas;

    @Override
    public void onEnable() {
        log = getLogger();
        ConfigAccessor configYml = new PluginConfigAccessor(this);

        gameMapManager = new GameMapManager(configYml, getDataFolder());
        gameMapManager.loadMaps();

        worldListener = new WorldListener();
        playerListener = new PlayerListener(this);

        saveDefaultConfig();
        saveDefaultConfigValues();
        loadConfig();
        loadArenas();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(worldListener, this);
        pm.registerEvents(playerListener, this);

        getCommand("quit").setExecutor(new QuitCommand(this));
    }

    @Override
    public void onDisable() {

    }

    public PlayerData getPlayerData(Player p) {
        return getPlayerData(p.getUniqueId());
    }

    public PlayerData getPlayerData(UUID id) {
        if (!players.containsKey(id))
            players.put(id, new PlayerData());
        return players.get(id);
    }
    
    public void removePlayerData(Player p) {
        removePlayerData(p.getUniqueId());
    }
    
    public void removePlayerData(UUID id) {
        players.remove(id);
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
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
        String gameWorldName = getConfig().getString("game-world");
        gameWorld = new WorldCreator(gameWorldName).generator(new EmptyChunkGenerator()).createWorld();

        // Load the lobby spawn location. Default is in world "lobby" at location (0,64,0). If the world doesn't exist,
        // create it to save ourselves the hassle of setting the thing up.
        String lobbyWorldName = getConfig().getString("spawn.world");
        World lobbyWorld = new WorldCreator(lobbyWorldName).generator(new EmptyChunkGenerator()).createWorld();
        double lobbyX = getConfig().getDouble("spawn.x");
        double lobbyY = getConfig().getDouble("spawn.y");
        double lobbyZ = getConfig().getDouble("spawn.z");
        lobbySpawn = new Location(lobbyWorld, lobbyX, lobbyY, lobbyZ);

        arenas = new ArenaManager(this, gameWorld, gameMapManager);
        arenas.loadArenas();
        
        // Iterate over the map lists in the config file with the sign locations
        List<Map<?, ?>> mapList = getConfig().getMapList("signs");
        for (Map<?, ?> map : mapList) {
            // Objects since we don't know what type these are yet
            Object xx = map.get("x");
            Object yy = map.get("y");
            Object zz = map.get("z");
            Object mapName = map.get("map");

            // Check that all the objects are numbers; if not, just skip this sign
            if (xx instanceof Number && yy instanceof Number && zz instanceof Number && mapName instanceof String) {
                int x = ((Number) xx).intValue();
                int y = ((Number) yy).intValue();
                int z = ((Number) zz).intValue();
                GameMap gameMap = gameMapManager.getMap((String) mapName);

                // Make sure the specified map exists
                if (gameMap == null)
                    continue;

                Location loc = new Location(lobbyWorld, x, y, z);
                Block b = loc.getBlock();
                if (!(b.getState() instanceof Sign))
                    placeSign(loc); // Build a new sign at the location (idiot-proofing)
                GameSign sign = new GameSign(b, (String) mapName);
                Game g = new Game(this, sign);
                sign.setGame(g);
                signs.put(loc, sign);
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

    private void loadArenas() {
        for (GameSign sign : signs.values()) {
            getLogger().info("Loading arena for game linked to sign at " + sign.getLocation());
            String mapName = sign.getMapName();
            sign.getGame().setArena(arenas.getArena(mapName));
        }
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
                    state.update(true);
                    break;
                }
            }
        }
    }
}
