package net.coasterman10.rangers.game;

import net.coasterman10.rangers.config.ConfigAccessor;
import net.coasterman10.rangers.config.PluginConfigAccessor;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

public class GameSettings {
    private final ConfigAccessor configYml;

    public int minPlayers;
    public int maxPlayers;
    public int countdownDuration;
    public int teamSelectTime;
    public int restartDelay;
    public int timeLimit;

    public GameSettings(Plugin plugin) {
        configYml = new PluginConfigAccessor(plugin);
    }

    public void load() {
        configYml.reload();

        ConfigurationSection config = configYml.get();

        minPlayers = config.getInt("min-players", 2);
        maxPlayers = config.getInt("max-players", 10);
        countdownDuration = config.getInt("countdown-duration", 60);
        teamSelectTime = config.getInt("team-select-time", 10);
        restartDelay = config.getInt("restart-delay", 10);
        timeLimit = config.getInt("time-limit", 300);
    }
}
