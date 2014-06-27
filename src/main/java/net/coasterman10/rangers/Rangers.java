package net.coasterman10.rangers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Rangers extends JavaPlugin {
    // Guess what? I have the print margin at 120 characters. If you have a tiny screen or use a terminal for Java for
    // some insane reason, please make a pull request to reduce the margin to the 80 character mark.
    // It will be denied anyways but at least I'll know how many people care about it that much.
    // Without any further adieu, let us dive into the horrid mess that is code by coasterman10.

    private Location lobbySpawn;
    private World gameWorld;
    private Map<Location, GameSign> signs = new HashMap<>();
    private Map<Integer, Game> games = new HashMap<>();
    private Map<UUID, PlayerData> players = new HashMap<>();
    private Map<String, GameMap> maps = new HashMap<>();
    private FileConfiguration builtArenas;

    private WorldListener worldListener;
    private PlayerListener playerListener;

    @Override
    public void onEnable() {
        worldListener = new WorldListener();
        playerListener = new PlayerListener(this);

        saveDefaultConfig();
        saveDefaultConfigValues();
        loadConfig();
        loadGames();

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
        File lobbySchematicFile = new File(getDataFolder(), "schematics" + File.separator
                + getConfig().getString("game-lobby.schematic"));
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
            map.rangerSpawn = getVector(getConfig().getConfigurationSection("maps." + mapName + ".spawns.rangers"));
            map.banditSpawn = getVector(getConfig().getConfigurationSection("maps." + mapName + ".spawns.bandits"));
            map.rangerHopper = getVector(getConfig().getConfigurationSection("maps." + mapName + ".hoppers.rangers"))
                    .toBlockVector();
            map.banditHopper = getVector(getConfig().getConfigurationSection("maps." + mapName + ".hoppers.bandits"))
                    .toBlockVector();
            map.lobbySpawn = gameLobbySpawn;
            maps.put(mapName, map);
        }

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
                GameSign sign = new GameSign(b, gameMap);
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

    @SuppressWarnings("unchecked")
    private void loadGames() {
        // TODO: Track location of both lobbies and arenas
        // I just realized that this is a rather shoddy method to handle arena building, and instead the config will
        // have to hold the locations of both the lobbies AND to be safe in case I ever decide to change the distance of
        // the arena from the lobby.

        // This file contains all of the arenas that have already been built by the plugin.
        builtArenas = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "built-arenas.yml"));
        Collection<Vector> usedArenas = new HashSet<>(); // Keeps track of arenas already in use by another game
        for (GameSign sign : signs.values()) {
            getLogger().info("Loading game for sign at " + sign.getLocation());

            String mapName = sign.getMap().name;

            // Arenas built for the map assigned to the game that we want this sign to connect to
            List<Object> list = (List<Object>) builtArenas.getList(mapName, new ArrayList<Vector>());
            List<Vector> arenas = new ArrayList<>();

            // Since we can't get a vector list with the Bukkit API, we have to cast the objects ourselves
            for (Object o : list)
                if (o instanceof Vector)
                    arenas.add((Vector) o);

            // Loop through all the arenas that have the map we want, and create the game if the arena is found
            boolean foundArena = false;
            for (Vector v : arenas) {
                if (!usedArenas.contains(v)) {
                    Game g = new Game(this, sign, sign.getMap(), v.toLocation(gameWorld), v.toLocation(gameWorld).add(
                            0, 0, 1000));
                    usedArenas.add(v);
                    foundArena = true;
                    games.put(g.getId(), g);
                    break;
                }
            }

            if (foundArena)
                continue;

            // We have not found any open arenas with the map we want, so it is time to build a new one.
            // The new arena is built 1000 blocks further from the origin than the furthest one in the X direction.
            Vector newArena = new Vector(0, 64, 0);
            for (Vector v : arenas) {
                if (v.getBlockX() > newArena.getX() + 1000)
                    newArena.setX(v.getBlockX() + 1000);
            }

            getLogger().info("Building new arena at " + newArena.toLocation(gameWorld));

            usedArenas.add(newArena);
            list.add(newArena);
            builtArenas.set(mapName, list);

            // Create the new game, then build the schematic.
            // The new arena is built 1000 blocks from the lobby, in the Z direction.
            Game g = new Game(this, sign, sign.getMap(), newArena.toLocation(gameWorld), newArena.toLocation(gameWorld)
                    .add(0, 0, 1000));
            games.put(g.getId(), g);
            g.getMap().gameSchematic.buildDelayed(newArena.toLocation(gameWorld).add(0, 0, 1000), this);
        }
        try {
            builtArenas.save(new File(getDataFolder(), "built-arenas.yml"));
        } catch (IOException e) {
            getLogger().log(Level.WARNING, e.getMessage(), e);
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
