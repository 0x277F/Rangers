package net.coasterman10.rangers;

import net.coasterman10.rangers.config.ConfigAccessor;
import net.coasterman10.rangers.config.PluginConfigAccessor;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class CommonSettings {
    private final ConfigAccessor configYml;

    public int minPlayers;
    public int maxPlayers;
    public int countdownDuration;
    public int lockTime;
    public int teamSelectTime;
    public int restartDelay;

    public CommonSettings(Plugin plugin) {
        configYml = new PluginConfigAccessor(plugin);
    }

    public void load() {
        configYml.reload();

        FileConfiguration config = configYml.get();

        minPlayers = config.getInt("min-players", 2);
        maxPlayers = config.getInt("max-players", 10);
        countdownDuration = config.getInt("countdown-duration", 60);
        lockTime = config.getInt("lock-time", 10);
        teamSelectTime = config.getInt("team-select-time", 10);
        restartDelay = config.getInt("restart-delay", 10);
    }
}
