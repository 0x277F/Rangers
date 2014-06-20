package net.coasterman10.rangers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Rangers extends JavaPlugin implements Listener {
    // Guess what? I have the print margin at 120 characters. If you have a tiny screen or use a terminal for Java for
    // some insane reason, please make a pull request to reduce the margin to the 80 character mark.
    // It will be denied anyways but at least I'll know how many people care about it that much.
    // Without any further adieu, let us dive into the horrid mess that is code by coasterman10.

    private Location lobby;
    private Map<Integer, Game> games = new HashMap<>();
    private Map<Location, GameSign> signs = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        // TODO: Literally copy defaults to the actual config.yml so idiots who break their config.ymls can fix them
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        loadConfig();

        for (Entry<Location, GameSign> entry : signs.entrySet()) {
            Game g = new Game(entry.getValue());
            games.put(g.getId(), g);
            entry.getValue().setGame(g);
        }
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.getPlayer().sendMessage("Welcome to Rangers!");
        e.getPlayer().teleport(lobby);
        e.getPlayer().getInventory().clear();
        e.getPlayer().getInventory().setArmorContents(null); // Essentials idiot devs still haven't figured this out
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        Location loc = e.getClickedBlock().getLocation();
        if (signs.containsKey(loc)) {
            if (!signs.get(loc).getGame().addPlayer(e.getPlayer().getUniqueId())) {
                e.getPlayer().sendMessage(ChatColor.RED + "That game is full!");
            }
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent e) {
        if (e.getEntity().getItemStack().getType() == Material.SKULL)
            e.setCancelled(true); // Prevent player heads from despawning
    }

    private void loadConfig() {
        // Load the lobby spawn location. Default is in world "lobby" at location (0,64,0). If the world doesn't exist,
        // create it to save ourselves the hassle of setting the thing up.
        String lobbyWorldName = getConfig().getString("lobby.world");
        World lobbyWorld = new WorldCreator(lobbyWorldName).generator(new EmptyChunkGenerator()).createWorld();
        int lobbyX = getConfig().getInt("lobby.x");
        int lobbyY = getConfig().getInt("lobby.y");
        int lobbyZ = getConfig().getInt("lobby.z");
        lobby = new Location(lobbyWorld, lobbyX, lobbyY, lobbyZ);

        // Iterate over the list of maps in the config file with the sign locations
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
                signs.put(loc, new GameSign(b));
            }
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
                    state.update();
                    break;
                }
            }
        }
    }
}
