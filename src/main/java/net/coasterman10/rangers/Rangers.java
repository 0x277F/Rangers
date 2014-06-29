package net.coasterman10.rangers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.coasterman10.rangers.listeners.HopperListener;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Rangers extends JavaPlugin {
    private Location lobbySpawn;
    private World gameWorld;
    private Map<Location, GameSign> signs = new HashMap<>();
    private Map<UUID, PlayerData> players = new HashMap<>();
    private Map<String, GameMap> maps = new HashMap<>();

    private WorldListener worldListener;
    private PlayerListener playerListener;
    private HopperListener hopperListener;
    private ArenaManager arenas;

    @Override
    public void onEnable() {
        worldListener = new WorldListener();
        playerListener = new PlayerListener(this);
        hopperListener = new HopperListener();

        saveDefaultConfig();
        saveDefaultConfigValues();
        loadConfig();
        loadArenas();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(worldListener, this);
        pm.registerEvents(playerListener, this);
        pm.registerEvents(hopperListener, this);

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

        // Schematic for the game lobbies
        String lobbySchematicFilename = "schematics" + File.separator + getConfig().getString("game-lobby.schematic");
        File lobbySchematicFile = new File(getDataFolder(), lobbySchematicFilename);
        Schematic lobbySchematic;
        try {
            lobbySchematic = new Schematic(lobbySchematicFile);
        } catch (IOException | InvalidSchematicException e) {
            getLogger().warning("Could not load game lobby schematic: " + e.getMessage());
            getLogger().warning("Loading default empty schematic for game lobby.");
            lobbySchematic = new Schematic();
        }

        // Vector containing the offset of the lobby spawn from its origin
        Vector gameLobbySpawn = getVector(getConfig().getConfigurationSection("game-lobby.spawn"));

        // Load the game map configurations
        for (String mapName : getConfig().getConfigurationSection("maps").getKeys(false)) {
            ConfigurationSection section = getConfig().getConfigurationSection("maps." + mapName);
            GameMap map = new GameMap(mapName);
            map.lobbySchematic = lobbySchematic;
            try {
                map.gameSchematic = new Schematic(new File(getDataFolder(), "schematics" + File.separator
                        + getConfig().getString("maps." + mapName + ".schematic")));
            } catch (IOException | InvalidSchematicException e) {
                getLogger().warning("Could not load game map schematic for map " + mapName + ": " + e.getMessage());
                getLogger().warning("Loading default empty schematic for map " + mapName);
                map.gameSchematic = new Schematic();
            }
            map.rangerSpawn = getVector(section.getConfigurationSection("spawns.rangers"));
            map.banditSpawn = getVector(section.getConfigurationSection("spawns.bandits"));
            map.rangerHopper = getVector(section.getConfigurationSection("hoppers.rangers")).toBlockVector();
            map.banditHopper = getVector(section.getConfigurationSection("hoppers.bandits")).toBlockVector();
            map.lobbySpawn = gameLobbySpawn;
            maps.put(mapName, map);
        }
        
        arenas = new ArenaManager(this, gameWorld, maps);

        // This will be passed to the hopper listener so it can check all the games
        Collection<Game> games = new HashSet<>();
        
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
                GameMap gameMap = maps.get(mapName);

                // Make sure the specified map exists
                if (gameMap == null)
                    continue;

                Location loc = new Location(lobbyWorld, x, y, z);
                Block b = loc.getBlock();
                if (!(b.getState() instanceof Sign))
                    placeSign(loc); // Build a new sign at the location (idiot-proofing)
                GameSign sign = new GameSign(b);
                Game g = new Game(this, sign, gameMap);
                sign.setGame(g);
                games.add(g);
                signs.put(loc, sign);
            }
        }
        playerListener.setSigns(signs);
        hopperListener.setGames(games);

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
            String mapName = sign.getGame().getMap().name;
            sign.getGame().setArena(arenas.getArena(mapName));
        }
    }

    private static Vector getVector(ConfigurationSection config) {
        if (config.isSet("x") && config.isSet("y") && config.isSet("z"))
            return new Vector(config.getDouble("x"), config.getDouble("y"), config.getDouble("z"));
        else
            return new Vector();
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
