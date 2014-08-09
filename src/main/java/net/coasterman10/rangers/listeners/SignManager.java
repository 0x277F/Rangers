package net.coasterman10.rangers.listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import net.coasterman10.rangers.Game;
import net.coasterman10.rangers.GameJoinSign;
import net.coasterman10.rangers.GameSign;
import net.coasterman10.rangers.GameStatusSign;
import net.coasterman10.rangers.PlayerManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SignManager implements Listener {
    private Collection<GameSign> signs = new LinkedList<>();
    private Map<Location, GameJoinSign> joinSigns = new HashMap<>();

    public SignManager(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (GameSign s : signs) {
                    s.update();
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    public void addJoinSign(Game g, Location loc) {
        if (!(loc.getBlock().getState() instanceof Sign))
            placeSign(loc);
        GameJoinSign s = new GameJoinSign(loc);
        s.setGame(g);
        signs.add(s);
        joinSigns.put(loc, s);
    }
    
    public void addStatusSign(Game g, Location loc) {
        if (!(loc.getBlock().getState() instanceof Sign))
            placeSign(loc);
        GameStatusSign s = new GameStatusSign(loc);
        s.setGame(g);
        signs.add(s);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (joinSigns.containsKey(e.getClickedBlock().getLocation())) {
                Game g = joinSigns.get(e.getClickedBlock().getLocation()).getGame();
                if (g != null) {
                    g.addPlayer(PlayerManager.getPlayer(e.getPlayer()));
                }
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
                    state.update(true);
                    break;
                }
            }
        }
    }
}
