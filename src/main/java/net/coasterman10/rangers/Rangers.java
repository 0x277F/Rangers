package net.coasterman10.rangers;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.boss.DebugBossSpawnCommand;
import net.coasterman10.rangers.command.QuitCommand;
import net.coasterman10.rangers.command.SubcommandExecutor;
import net.coasterman10.rangers.command.arena.ArenaAddCommand;
import net.coasterman10.rangers.command.arena.ArenaListCommand;
import net.coasterman10.rangers.command.arena.ArenaRemoveCommand;
import net.coasterman10.rangers.command.arena.ArenaSetChestCommand;
import net.coasterman10.rangers.command.arena.ArenaSetMaxCommand;
import net.coasterman10.rangers.command.arena.ArenaSetMinCommand;
import net.coasterman10.rangers.command.arena.ArenaSetNameCommand;
import net.coasterman10.rangers.command.arena.ArenaSetSpawnCommand;
import net.coasterman10.rangers.command.rangers.RangersReloadCommand;
import net.coasterman10.rangers.command.rangers.RangersSettingCommand;
import net.coasterman10.rangers.config.ConfigAccessor;
import net.coasterman10.rangers.config.ConfigSectionAccessor;
import net.coasterman10.rangers.config.PluginConfigAccessor;
import net.coasterman10.rangers.game.Game;
import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.game.GameSettings;
import net.coasterman10.rangers.listeners.AbilityListener;
import net.coasterman10.rangers.listeners.MenuManager;
import net.coasterman10.rangers.listeners.PlayerListener;
import net.coasterman10.rangers.listeners.SignManager;
import net.coasterman10.rangers.listeners.WorldListener;
import net.coasterman10.rangers.menu.BanditBowMenu;
import net.coasterman10.rangers.menu.BanditSecondaryMenu;
import net.coasterman10.rangers.menu.RangerAbilityMenu;
import net.coasterman10.rangers.menu.RangerBowMenu;
import net.coasterman10.rangers.menu.RangerSecondaryMenu;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Rangers extends JavaPlugin {
    private static Logger log;

    public static Logger logger() {
        return log;
    }

    private Location lobbySpawn;

    private WorldListener worldListener;
    private PlayerListener playerListener;
    private AbilityListener abilityListener;
    private SignManager signManager;
    private MenuManager menuManager;
    private ArenaManager arenaManager;

    private GameSettings settings;

    @Override
    public void onEnable() {
        log = getLogger();
        ConfigAccessor configYml = new PluginConfigAccessor(this);

        arenaManager = new ArenaManager(new ConfigSectionAccessor(configYml, "arenas"));
        worldListener = new WorldListener();
        playerListener = new PlayerListener(this);
        abilityListener = new AbilityListener(this);
        signManager = new SignManager(arenaManager, new ConfigSectionAccessor(configYml, "signs"));
        menuManager = new MenuManager();

        settings = new GameSettings(configYml);

        saveDefaultConfig();
        loadConfig();

        menuManager.addSignMenu(new RangerAbilityMenu(),
                new SignText().setLine(1, "Select").setLine(2, "Ranger Ability"));
        menuManager.addSignMenu(new RangerBowMenu(),
                new SignText().setLine(1, "Select Ranger").setLine(2, "Bow Upgrades"));
        menuManager.addSignMenu(new RangerSecondaryMenu(),
                new SignText().setLine(1, "Select Ranger").setLine(2, "Secondary"));
        menuManager.addSignMenu(new BanditSecondaryMenu(),
                new SignText().setLine(1, "Select Bandit").setLine(2, "Secondary"));
        menuManager.addSignMenu(new BanditBowMenu(),
                new SignText().setLine(1, "Select Bandit").setLine(2, "Bow Upgrades"));

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(worldListener, this);
        pm.registerEvents(playerListener, this);
        pm.registerEvents(abilityListener, this);
        pm.registerEvents(signManager, this);
        pm.registerEvents(menuManager, this);

        SubcommandExecutor rangersCommand = new SubcommandExecutor("rangers");
        rangersCommand.registerSubcommand(new RangersSettingCommand(settings));
        rangersCommand.registerSubcommand(new RangersReloadCommand(this));

        SubcommandExecutor arenaCommand = new SubcommandExecutor("arena");
        arenaCommand.registerSubcommand(new ArenaAddCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaRemoveCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaListCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaSetNameCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaSetMinCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaSetMaxCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaSetSpawnCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaSetChestCommand(arenaManager));

        getCommand("rangers").setExecutor(rangersCommand);
        getCommand("arena").setExecutor(arenaCommand);
        getCommand("quit").setExecutor(new QuitCommand(this));
        getCommand("spawnboss").setExecutor(new DebugBossSpawnCommand());
    }

    @Override
    public void onDisable() {

    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public void sendToLobby(Player p) {
        GamePlayer data = PlayerManager.getPlayer(p);
        if (data.getGame() != null) {
            data.getGame().removePlayer(data);
        }
        p.setHealth(20D);
        p.setFoodLevel(20);
        p.setSaturation(20F);
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        p.setExp(0F);
        if (p.getGameMode() != GameMode.CREATIVE)
            p.setAllowFlight(false);
        p.teleport(lobbySpawn);
    }

    private void loadConfig() {
        // Load the lobby spawn location. Default is in world "lobby" at location (0,64,0). If the world doesn't exist,
        // create it to save ourselves the hassle of setting the thing up.
        String lobbyWorldName = getConfig().getString("spawn.world");
        World lobbyWorld = new WorldCreator(lobbyWorldName).generator(new EmptyChunkGenerator()).createWorld();
        double lobbyX = getConfig().getDouble("spawn.x");
        double lobbyY = getConfig().getDouble("spawn.y");
        double lobbyZ = getConfig().getDouble("spawn.z");
        lobbySpawn = new Location(lobbyWorld, lobbyX, lobbyY, lobbyZ);
        if (getConfig().contains("spawn.yaw"))
            lobbySpawn.setYaw((float) getConfig().getDouble("spawn.yaw"));
        if (getConfig().contains("spawn.pitch"))
            lobbySpawn.setPitch((float) getConfig().getDouble("spawn.pitch"));

        arenaManager.loadArenas();

        // Load the games for each arena
        for (Arena a : arenaManager.getArenas()) {
            Game g = new Game(this, settings);
            g.setArena(a);
        }

        // Game Settings - this is the alternative to global variables
        settings.load();

        // Load the signs to join the arenas
        signManager.loadSigns();

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

    private static Vector parseVector(Map<?, ?> map) {
        Object x = map.get("x");
        Object y = map.get("y");
        Object z = map.get("z");

        if (x instanceof Number && y instanceof Number && z instanceof Number) {
            return new Vector(((Number) x).doubleValue(), ((Number) y).doubleValue(), ((Number) z).doubleValue());
        } else {
            return null;
        }
    }
}
