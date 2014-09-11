package net.coasterman10.rangers;

import java.util.EnumMap;
import java.util.Map;

import net.coasterman10.rangers.game.Game.State;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public class GameStatusSign extends GameSign {
    private static final Map<State, String> STATUS_TEXT = new EnumMap<>(State.class);

    static {
        STATUS_TEXT.put(State.INACTIVE, "Inactive");
        STATUS_TEXT.put(State.LOBBY, "In Lobby");
        STATUS_TEXT.put(State.STARTING, "In Lobby");
        STATUS_TEXT.put(State.RUNNING, "Running");
        STATUS_TEXT.put(State.ENDING, "Ending");
    }

    public GameStatusSign(Location location) {
        super(location);
    }

    public void update() {
        if (game != null) {
            setLine(0,
                    (game.getPlayerCount() == game.getSettings().maxPlayers ? ChatColor.RED.toString() : "")
                            + game.getPlayerCount() + " / " + game.getSettings().maxPlayers);
            setLine(1, STATUS_TEXT.get(game.getState()));
            switch (game.getState()) {
            case INACTIVE:
                setLine(2, "No Arena Set");
                setLine(3, "Misconfigured");
                break;
            case LOBBY:
                setLine(2, game.getSettings().minPlayers - game.getPlayerCount() + " more needed");
                setLine(3, "");
                break;
            case STARTING:
                setLine(2, "Starting in");
                setLine(3, formatTime(game.getSeconds()));
                break;
            case RUNNING:
                setLine(2, "Running");
                setLine(3, formatTime(game.getSeconds()));
                break;
            case ENDING:
                setLine(2, "Restarting in");
                setLine(3, formatTime(game.getSeconds()));
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
