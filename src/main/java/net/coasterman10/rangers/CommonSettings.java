package net.coasterman10.rangers;

import java.io.File;

import net.coasterman10.rangers.config.ConfigAccessor;
import net.coasterman10.rangers.config.FileConfigAccessor;
import net.coasterman10.rangers.config.PluginConfigAccessor;
import net.coasterman10.rangers.kits.Kit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class CommonSettings {
    private final ConfigAccessor configYml;
    private final ConfigAccessor kitsYml;

    private Kit rangerKit = new Kit();
    private Kit banditKit = new Kit();

    public int minPlayers;
    public int maxPlayers;
    public int countdownDuration;
    public int lockTime;
    public int teamSelectTime;
    public int restartDelay;

    public CommonSettings(Plugin plugin) {
        configYml = new PluginConfigAccessor(plugin);
        kitsYml = new FileConfigAccessor(new File(plugin.getDataFolder(), "kits.yml"), plugin.getResource("kits.yml"));
    }

    public void load() {
        configYml.reload();
        kitsYml.reload();

        FileConfiguration config = configYml.get();
        FileConfiguration kitConfig = kitsYml.get();

        minPlayers = config.getInt("min-players", 2);
        maxPlayers = config.getInt("max-players", 10);
        countdownDuration = config.getInt("countdown-duration", 60);
        lockTime = config.getInt("lock-time", 10);
        teamSelectTime = config.getInt("team-select-time", 10);
        restartDelay = config.getInt("restart-delay", 10);

        rangerKit.load(kitConfig.getConfigurationSection("rangers"));
        banditKit.load(kitConfig.getConfigurationSection("bandits"));
    }

    public Kit getRangerKit() {
        return rangerKit;
    }

    public Kit getBanditKit() {
        return banditKit;
    }

    public int getInt(String path) {
        return configYml.get().getInt(path);
    }
}
