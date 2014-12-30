package net.coasterman10.rangers.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.coasterman10.rangers.ArenaJoinSign;
import net.coasterman10.rangers.ArenaSign;
import net.coasterman10.rangers.ArenaStatusSign;
import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.player.RangersPlayer;
import net.coasterman10.rangers.util.ConfigAccessor;
import net.coasterman10.rangers.util.ConfigUtil;

import org.bukkit.Location;
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
    private Map<Arena, Collection<ArenaSign>> signs = new HashMap<>();
    private Map<Location, ArenaSign> signLocations = new HashMap<>();

    public SignManager(ArenaManager arenaManager, ConfigAccessor config) {
        this.arenaManager = arenaManager;
        this.config = config;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (signLocations.containsKey(e.getClickedBlock().getLocation())) {
                ArenaSign s = signLocations.get(e.getClickedBlock().getLocation());
                if (s instanceof ArenaJoinSign && s.hasArena()) {
                    RangersPlayer.getPlayer(e.getPlayer()).joinArena(s.getArena());
                }
            }
        }
    }

    public void loadSigns() {
        config.reload();
        ConfigurationSection conf = config.get();

        for (String name : conf.getKeys(false)) {
            Arena arena = arenaManager.getArena(name);
            if (arena == null)
                continue;
            ConfigurationSection signConf = conf.getConfigurationSection(name);
            if (signConf == null)
                continue;
            for (Location joinSign : ConfigUtil.getLocationList(signConf, "join")) {
                addJoinSign(arena, joinSign, false);
            }
            for (Location statusSign : ConfigUtil.getLocationList(signConf, "status")) {
                addStatusSign(arena, statusSign, false);
            }
        }
    }
    
    public void saveSigns() {
        ConfigurationSection conf = config.get();
        for (Entry<Arena, Collection<ArenaSign>> entry : signs.entrySet()) {
            Arena arena = entry.getKey();
            List<Location> joinSigns = new ArrayList<>();
            List<Location> statusSigns = new ArrayList<>();
            for (ArenaSign sign : entry.getValue()) {
                if (sign instanceof ArenaJoinSign) {
                    joinSigns.add(sign.getLocation());
                } else {
                    statusSigns.add(sign.getLocation());
                }
            }
            ConfigUtil.setLocationList(conf, arena.getName() + ".join", joinSigns);
            ConfigUtil.setLocationList(conf, arena.getName() + ".status", statusSigns);
        }
        // Clean up any rogue sections caused by an arena being renamed
        for (String key : conf.getKeys(false)) {
            if (arenaManager.getArena(key) == null) {
                conf.set(key, null);
            }
        }
        config.save();
    }

    public void update() {
        for (ArenaSign s : signLocations.values()) {
            s.update();
        }
    }

    public void addJoinSign(Arena arena, Location loc) {
        addJoinSign(arena, loc, true);
    }

    public void addStatusSign(Arena arena, Location loc) {
        addStatusSign(arena, loc, true);
    }

    private void addJoinSign(Arena arena, Location loc, boolean save) {
        ArenaJoinSign sign = new ArenaJoinSign(arena, loc);
        addSign(arena, sign);
        if (save)
            saveSigns();
    }

    private void addStatusSign(Arena arena, Location loc, boolean save) {
        ArenaStatusSign sign = new ArenaStatusSign(arena, loc);
        addSign(arena, sign);
        if (save)
            saveSigns();
    }

    private void addSign(Arena arena, ArenaSign sign) {
        Collection<ArenaSign> signsForArena = signs.get(arena);
        if (signsForArena == null) {
            signsForArena = new HashSet<>();
            signs.put(arena, signsForArena);
        }
        signsForArena.add(sign);
        signLocations.put(sign.getLocation(), sign);
    }

    public boolean removeSign(Location loc) {
        ArenaSign sign = signLocations.get(loc);
        if (sign == null)
            return false;
        signLocations.remove(loc);
        if (signs.containsKey(sign.getArena()))
            signs.get(sign.getArena()).remove(sign);
        BlockState state = loc.getBlock().getState();
        if (state instanceof Sign) {
            for (int i = 0; i < 4; i++)
                ((Sign) state).setLine(i, "");
            state.update(true);
        }
        saveSigns();
        return true;
    }
}
