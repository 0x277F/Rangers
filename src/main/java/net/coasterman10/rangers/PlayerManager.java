package net.coasterman10.rangers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class PlayerManager {
    private static PlayerManager instance = new PlayerManager();

    public static PlayerManager instance() {
        return instance;
    }
    
    public static GamePlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }
    
    public static GamePlayer getPlayer(UUID id) {
        return instance._getPlayer(id);
    }
    
    public static void removePlayer(Player player) {
        removePlayer(player.getUniqueId());
    }
    
    public static void removePlayer(UUID id) {
        instance._removePlayer(id);
    }

    public static boolean isInGame(Player player){
        return instance.players.containsKey(player.getUniqueId());
    }

    private Map<UUID, GamePlayer> players = new HashMap<>();
    
    private GamePlayer _getPlayer(UUID id) {
        GamePlayer player = players.get(id);
        if (player == null) {
            player = new GamePlayer(id);
            player.loadData();
            players.put(id, player);
        }
        return player;
    }
    
    private void _removePlayer(UUID id) {
        GamePlayer player = players.get(id);
        if (player != null) {
            player.saveData();
            players.remove(id);
        }
    }
}
