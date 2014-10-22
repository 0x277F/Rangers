package net.coasterman10.rangers.game;

import net.coasterman10.rangers.config.ConfigAccessor;

import org.bukkit.configuration.ConfigurationSection;

public class GameSettings {
    private final ConfigAccessor config;

    public int minPlayers;
    public int maxPlayers;
    public int countdownDuration;
    public int teamSelectTime;
    public int restartDelay;
    public int timeLimit;

    public GameSettings(ConfigAccessor config) {
        this.config = config;
    }

    public void load() {
        config.reload();

        ConfigurationSection conf = config.get();

        minPlayers = conf.getInt("min-players", 2);
        maxPlayers = conf.getInt("max-players", 10);
        countdownDuration = conf.getInt("countdown-duration", 60);
        teamSelectTime = conf.getInt("team-select-time", 10);
        restartDelay = conf.getInt("restart-delay", 10);
        timeLimit = conf.getInt("time-limit", 300);
    }
}
