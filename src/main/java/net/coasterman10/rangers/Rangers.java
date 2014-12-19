package net.coasterman10.rangers;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import me.confuser.barapi.BarAPI;
import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.arena.ClassicArena;
import net.coasterman10.rangers.boss.SpawnBossSubcommand;
import net.coasterman10.rangers.command.QuitCommand;
import net.coasterman10.rangers.command.SubcommandExecutor;
import net.coasterman10.rangers.command.arena.ArenaAddCommand;
import net.coasterman10.rangers.command.arena.ArenaJoinCommand;
import net.coasterman10.rangers.command.arena.ArenaListCommand;
import net.coasterman10.rangers.command.arena.ArenaRemoveCommand;
import net.coasterman10.rangers.command.arena.ArenaSetChestCommand;
import net.coasterman10.rangers.command.arena.ArenaSetMaxCommand;
import net.coasterman10.rangers.command.arena.ArenaSetMinCommand;
import net.coasterman10.rangers.command.arena.ArenaSetNameCommand;
import net.coasterman10.rangers.command.arena.ArenaSetSpawnCommand;
import net.coasterman10.rangers.command.rangers.RangersReloadCommand;
import net.coasterman10.rangers.command.rangers.RangersSettingCommand;
import net.coasterman10.rangers.command.sign.SignAddCommand;
import net.coasterman10.rangers.command.sign.SignRemoveCommand;
import net.coasterman10.rangers.config.ConfigAccessor;
import net.coasterman10.rangers.config.ConfigSectionAccessor;
import net.coasterman10.rangers.config.PluginConfigAccessor;
import net.coasterman10.rangers.game.ClassicGame;
import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.game.GameSettings;
import net.coasterman10.rangers.listeners.AbilityListener;
import net.coasterman10.rangers.listeners.MenuManager;
import net.coasterman10.rangers.listeners.PlayerDeathListener;
import net.coasterman10.rangers.listeners.PlayerListener;
import net.coasterman10.rangers.listeners.SignManager;
import net.coasterman10.rangers.listeners.WorldListener;
import net.coasterman10.rangers.menu.BanditAbilityMenu;
import net.coasterman10.rangers.menu.BanditBowMenu;
import net.coasterman10.rangers.menu.BanditSecondaryMenu;
import net.coasterman10.rangers.menu.RangerAbilityMenu;
import net.coasterman10.rangers.menu.RangerBowMenu;
import net.coasterman10.rangers.menu.RangerSecondaryMenu;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Rangers extends JavaPlugin {
    private static Rangers instance;

    public static Rangers instance() {
        return instance;
    }

    private Location lobbySpawn;

    private WorldListener worldListener;
    private PlayerListener playerListener;
    private AbilityListener abilityListener;
    private PlayerDeathListener playerDeathListener;
    private SignManager signManager;
    private MenuManager menuManager;
    private ArenaManager arenaManager;

    private GameSettings settings;

    @Override
    public void onEnable() {
        instance = this;

        ConfigAccessor configYml = new PluginConfigAccessor(this);

        arenaManager = new ArenaManager(new ConfigSectionAccessor(configYml, "arenas"));
        worldListener = new WorldListener();
        playerListener = new PlayerListener(this);
        abilityListener = new AbilityListener(this);
        playerDeathListener = new PlayerDeathListener();
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
        menuManager.addSignMenu(new BanditAbilityMenu(),
                new SignText().setLine(1, "Select").setLine(2, "Bandit Ability"));
        menuManager.addSignMenu(new BanditBowMenu(),
                new SignText().setLine(1, "Select Bandit").setLine(2, "Bow Upgrades"));
        menuManager.addSignMenu(new BanditSecondaryMenu(),
                new SignText().setLine(1, "Select Bandit").setLine(2, "Secondary"));

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(worldListener, this);
        pm.registerEvents(playerListener, this);
        pm.registerEvents(abilityListener, this);
        pm.registerEvents(signManager, this);
        pm.registerEvents(menuManager, this);
        pm.registerEvents(playerDeathListener, this);

        SubcommandExecutor rangersCommand = new SubcommandExecutor("rangers");
        rangersCommand.registerSubcommand(new RangersSettingCommand(settings));
        rangersCommand.registerSubcommand(new RangersReloadCommand(this));
        rangersCommand.registerSubcommand(new SpawnBossSubcommand());

        SubcommandExecutor arenaCommand = new SubcommandExecutor("arena");
        arenaCommand.registerSubcommand(new ArenaAddCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaRemoveCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaListCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaSetNameCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaSetMinCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaSetMaxCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaSetSpawnCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaSetChestCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaJoinCommand(arenaManager));

        SubcommandExecutor signCommand = new SubcommandExecutor("sign");
        signCommand.registerSubcommand(new SignAddCommand(signManager, arenaManager));
        signCommand.registerSubcommand(new SignRemoveCommand(signManager));

        getCommand("rangers").setExecutor(rangersCommand);
        getCommand("arena").setExecutor(arenaCommand);
        getCommand("sign").setExecutor(signCommand);
        getCommand("quit").setExecutor(new QuitCommand(this));

        new BukkitRunnable() {
            @Override
            public void run() {
                signManager.update();
            }
        }.runTaskTimer(this, 0L, 10L);

        for (Player p : Bukkit.getOnlinePlayers()) {
            BarAPI.setMessage(p, settings.idleBarMessage, 100F);
        }
    }

    @Override
    public void onDisable() {
        instance = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            sendToLobby(p);
        }
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public void sendToLobby(Player p) {
        GamePlayer player = PlayerManager.getPlayer(p);
        if (player.isInGame()) {
            player.quit();
        }
        PlayerUtil.resetPlayer(p);
        p.teleport(lobbySpawn);
        BarAPI.setMessage(p, settings.idleBarMessage, 100F);
    }

    public void dropHead(Player player) {
        playerDeathListener.dropHead(player);
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
            if (a instanceof ClassicArena) {
                ClassicGame cg = new ClassicGame(settings, (ClassicArena) a, this);
                getServer().getPluginManager().registerEvents(cg, this);
            }
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
        playerDeathListener.setAllowedDrops(allowedDrops);
    }
}
