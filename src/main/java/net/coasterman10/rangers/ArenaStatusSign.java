package net.coasterman10.rangers;

import java.util.EnumMap;
import java.util.Map;

import net.coasterman10.rangers.arena.Arena;
import net.coasterman10.rangers.arena.GameState;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public class ArenaStatusSign extends ArenaSign {
    private static final Map<GameState, String> STATUS_TEXT = new EnumMap<>(GameState.class);

    static {
        STATUS_TEXT.put(GameState.LOBBY, "In Lobby");
        STATUS_TEXT.put(GameState.STARTING, "In Lobby");
        STATUS_TEXT.put(GameState.RUNNING, "Running");
        STATUS_TEXT.put(GameState.ENDING, "Ending");
    }

    public ArenaStatusSign(Location location) {
        super(location);
    }

    public void update() {
        if (hasArena()) {
            Arena arena = getArena();
            setLine(0,
                    (arena.getPlayerCount() == arena.getMaxPlayers() ? ChatColor.RED.toString() : "")
                            + arena.getPlayerCount() + " / " + arena.getMaxPlayers());
            setLine(1, STATUS_TEXT.get(arena.getState()));
            switch (arena.getState()) {
            case LOBBY:
                setLine(2, arena.getMinPlayers() - arena.getPlayerCount() + " more needed");
                setLine(3, "");
                break;
            case STARTING:
                setLine(2, "Starting in");
                setLine(3, formatTime(arena.getSeconds()));
                break;
            case RUNNING:
                setLine(2, "Running");
                setLine(3, formatTime(arena.getSeconds()));
                break;
            case ENDING:
                setLine(2, "Restarting in");
                setLine(3, formatTime(arena.getSeconds()));
                break;
            }
        } else {
            setLine(0, "");
            setLine(1, "N / A");
            setLine(2, "");
            setLine(3, "");
        }
    }

    private String formatTime(int seconds) {
        return (seconds / 60) + "m " + (seconds % 60) + "s";
    }
}
