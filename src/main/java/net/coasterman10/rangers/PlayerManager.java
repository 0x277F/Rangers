package net.coasterman10.rangers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.coasterman10.rangers.game.GamePlayer;

import org.bukkit.entity.Player;

public class PlayerManager {
    private static PlayerManager instance;

    public static PlayerManager instance() {
        if (instance == null)
            instance = new PlayerManager();
        return instance;
    }
    
    public static GamePlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }
    
    public static GamePlayer getPlayer(UUID id) {
        GamePlayer player = instance().players.get(id);
        if (player == null) {
            player = new GamePlayer(id);
            instance().players.put(id, player);
        }
        return player;
    }
    
    public static void removePlayer(Player player) {
        removePlayer(player.getUniqueId());
    }
    
    public static void removePlayer(UUID id) {
        instance().players.remove(id);
    }

    public static boolean isPlayerInGame(Player player){
        return instance().players.containsKey(player.getUniqueId());
    }

    private Map<UUID, GamePlayer> players = new HashMap<>();
}
