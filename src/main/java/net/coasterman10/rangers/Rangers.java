package net.coasterman10.rangers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import me.confuser.barapi.BarAPI;
import net.coasterman10.rangers.arena.ArenaManager;
import net.coasterman10.rangers.boss.SpawnBossSubcommand;
import net.coasterman10.rangers.command.QuitCommand;
import net.coasterman10.rangers.command.SubcommandExecutor;
import net.coasterman10.rangers.command.arena.ArenaAddCommand;
import net.coasterman10.rangers.command.arena.ArenaJoinCommand;
import net.coasterman10.rangers.command.arena.ArenaListCommand;
import net.coasterman10.rangers.command.arena.ArenaRemoveCommand;
import net.coasterman10.rangers.command.arena.ArenaRenameCommand;
import net.coasterman10.rangers.command.arena.ArenaSetChestCommand;
import net.coasterman10.rangers.command.arena.ArenaSetMaxCommand;
import net.coasterman10.rangers.command.arena.ArenaSetMinCommand;
import net.coasterman10.rangers.command.arena.ArenaSetSpawnCommand;
import net.coasterman10.rangers.command.rangers.RangersReloadCommand;
import net.coasterman10.rangers.command.sign.SignAddCommand;
import net.coasterman10.rangers.command.sign.SignRemoveCommand;
import net.coasterman10.rangers.command.stats.StatsSetCommand;
import net.coasterman10.rangers.command.stats.StatsShowCommand;
import net.coasterman10.rangers.listeners.AbilityListener;
import net.coasterman10.rangers.listeners.MenuManager;
import net.coasterman10.rangers.listeners.PlayerDeathListener;
import net.coasterman10.rangers.listeners.PlayerListener;
import net.coasterman10.rangers.listeners.SignManager;
import net.coasterman10.rangers.listeners.WorldListener;
import net.coasterman10.rangers.menu.PreferenceMenu;
import net.coasterman10.rangers.player.PlayerData;
import net.coasterman10.rangers.player.RangersPlayer;
import net.coasterman10.rangers.stats.StatManager;
import net.coasterman10.rangers.util.ConfigAccessor;
import net.coasterman10.rangers.util.ConfigSectionAccessor;
import net.coasterman10.rangers.util.ConfigUtil;
import net.coasterman10.rangers.util.EmptyChunkGenerator;
import net.coasterman10.rangers.util.PluginConfigAccessor;

import org.apache.commons.collections4.iterators.LoopingIterator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Rangers extends JavaPlugin {
    private Location lobbySpawn;
    private String idleBarMessage;

    private WorldListener worldListener;
    private PlayerListener playerListener;
    private AbilityListener abilityListener;
    private PlayerDeathListener playerDeathListener;
    private SignManager signManager;
    private MenuManager menuManager;
    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        ConfigAccessor configYml = new PluginConfigAccessor(this);

        arenaManager = new ArenaManager(this, new File(getDataFolder(), "arenas"));
        worldListener = new WorldListener();
        playerListener = new PlayerListener(this);
        abilityListener = new AbilityListener(this);
        playerDeathListener = new PlayerDeathListener(this);
        signManager = new SignManager(arenaManager, new ConfigSectionAccessor(configYml, "signs"));
        menuManager = new MenuManager();

        saveDefaultConfig();
        loadConfig();

        PlayerData.initialize(this);
        RangersPlayer.initialize(this);

        StatManager.initialize(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(worldListener, this);
        pm.registerEvents(playerListener, this);
        pm.registerEvents(abilityListener, this);
        pm.registerEvents(signManager, this);
        pm.registerEvents(menuManager, this);
        pm.registerEvents(playerDeathListener, this);

        SubcommandExecutor rangersCommand = new SubcommandExecutor("rangers");
        rangersCommand.registerSubcommand(new RangersReloadCommand(this));
        rangersCommand.registerSubcommand(new SpawnBossSubcommand());

        SubcommandExecutor arenaCommand = new SubcommandExecutor("arena");
        arenaCommand.registerSubcommand(new ArenaAddCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaRemoveCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaListCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaRenameCommand(arenaManager, signManager));
        arenaCommand.registerSubcommand(new ArenaSetMinCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaSetMaxCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaSetSpawnCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaSetChestCommand(arenaManager));
        arenaCommand.registerSubcommand(new ArenaJoinCommand(arenaManager));

        SubcommandExecutor signCommand = new SubcommandExecutor("sign");
        signCommand.registerSubcommand(new SignAddCommand(signManager, arenaManager));
        signCommand.registerSubcommand(new SignRemoveCommand(signManager));

        SubcommandExecutor statsCommand = new SubcommandExecutor("stats");
        statsCommand.registerSubcommand(new StatsSetCommand());
        statsCommand.registerSubcommand(new StatsShowCommand());

        getCommand("rangers").setExecutor(rangersCommand);
        getCommand("arena").setExecutor(arenaCommand);
        getCommand("sign").setExecutor(signCommand);
        getCommand("quit").setExecutor(new QuitCommand(this));
        getCommand("stats").setExecutor(statsCommand);

        for (PreferenceMenu menu : PreferenceMenu.menus) {
            menuManager.addSignMenu(menu.getSignText(), menu);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                signManager.update();
            }
        }.runTaskTimer(this, 0L, 10L);

        for (Player p : Bukkit.getOnlinePlayers()) {
            BarAPI.setMessage(p, idleBarMessage, 100F);
        }
        
        // Continually update the safe locations of players, with a maximum of 10 players per tick.
        // The more players that join the server, the lower the resolution of the safe location system.
        // The maximum delay for a server of 100 players is 10 ticks between updates.
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<RangersPlayer> it = new LoopingIterator<>(RangersPlayer.getPlayers());
                int players = RangersPlayer.getPlayers().size();
                for (int i = 0; i < 10 && i < players; i++) {
                    it.next().updateSafeLocation();
                }
            }
        }.runTaskTimer(this, 0L, 1L);
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            sendToLobby(p);
        }
        try{
            StatManager.saveAll();
        } catch (IOException | InvalidConfigurationException e){
            getLogger().severe("Error occured while saving statistic files:");
            e.printStackTrace();
        }
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public String getIdleBarMessage() {
        return idleBarMessage;
    }

    public void sendToLobby(Player p) {
        RangersPlayer player = RangersPlayer.getPlayer(p);
        if (player.isInArena()) {
            player.quit();
        }
        p.teleport(lobbySpawn);
        BarAPI.setMessage(p, idleBarMessage, 100F);
    }

    private void loadConfig() {
        // Load up all the world we will need
        ConfigurationSection worlds = getConfig().getConfigurationSection("worlds");
        if (worlds != null) {
            for (String world : worlds.getKeys(false)) {
                WorldCreator wc = new WorldCreator(world);
                wc.generator(new EmptyChunkGenerator());
                @SuppressWarnings("deprecation")
                Environment env = Environment.getEnvironment(worlds.getInt(world + ".dimension", 0));
                wc.environment(env != null ? env : Environment.NORMAL);
                Bukkit.createWorld(wc);
            }
        }
        
        lobbySpawn = ConfigUtil.getLocation(getConfig(), "spawn");
        idleBarMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("idle-bar-message"));

        arenaManager.loadArenas();
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
        RangersPlayer.setAllowedDrops(allowedDrops);
    }

    public String getBarMessage() {
        return idleBarMessage;
    }
}
