package net.coasterman10.rangers.listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.coasterman10.rangers.ArenaJoinSign;
import net.coasterman10.rangers.ArenaSign;
import net.coasterman10.rangers.ArenaStatusSign;
import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.config.ConfigAccessor;
import net.coasterman10.rangers.config.ConfigUtil;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignManager implements Listener {
    private final ArenaManager arenaManager;
    private final ConfigAccessor config;
    private Collection<ArenaSign> signs = new HashSet<>();
    private Map<Location, ArenaJoinSign> joinSigns = new HashMap<>();

    public SignManager(ArenaManager arenaManager, ConfigAccessor config) {
        this.arenaManager = arenaManager;
        this.config = config;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (joinSigns.containsKey(e.getClickedBlock().getLocation())) {
                ArenaJoinSign s = joinSigns.get(e.getClickedBlock().getLocation());
                if (s.hasGame()) {
                    s.getGame().addPlayer(PlayerManager.getPlayer(e.getPlayer()));
                }
            }
        }
    }

    public void loadSigns() {
        config.reload();
        ConfigurationSection conf = config.get();

        for (String arena : conf.getKeys(false)) {
            Arena a = arenaManager.getArena(arena);
            if (a == null)
                continue;
            ConfigurationSection signConf = conf.getConfigurationSection(arena);
            if (signConf == null)
                continue;
            Location join = ConfigUtil.getLocation(signConf, "join");
            if (join != null) {
                addJoinSign(a, join, false);
            }
            Location status = ConfigUtil.getLocation(signConf, "status");
            if (status != null) {
                addStatusSign(a, status, false);
            }
        }
    }

    public void update() {
        for (ArenaSign s : signs)
            s.update();
    }

    public void addJoinSign(Arena a, Location loc) {
        addJoinSign(a, loc, true);
    }

    public void addStatusSign(Arena a, Location loc) {
        addStatusSign(a, loc, true);
    }

    private void addJoinSign(Arena a, Location loc, boolean save) {
        if (!(loc.getBlock().getState() instanceof Sign))
            placeSign(loc);
        ArenaJoinSign s = new ArenaJoinSign(loc);
        s.setArena(a);
        a.setActive(true);
        signs.add(s);
        joinSigns.put(loc, s);
        if (save) {
            ConfigurationSection conf = config.get().getConfigurationSection(a.getId());
            if (conf == null) {
                conf = config.get().createSection(a.getId());
            }
            ConfigUtil.setLocation(conf, "join", loc);
        }
    }

    private void addStatusSign(Arena a, Location loc, boolean save) {
        if (!(loc.getBlock().getState() instanceof Sign))
            placeSign(loc);
        ArenaStatusSign s = new ArenaStatusSign(loc);
        s.setArena(a);
        signs.add(s);
        if (save) {
            ConfigurationSection conf = config.get().getConfigurationSection(a.getId());
            if (conf == null) {
                conf = config.get().createSection(a.getId());
            }
            ConfigUtil.setLocation(conf, "status", loc);
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
