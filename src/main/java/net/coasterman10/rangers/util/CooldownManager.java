package net.coasterman10.rangers.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class CooldownManager {
    private final Map<UUID, Long> startTimes = new HashMap<>();
    private int duration;
    
    public CooldownManager(int duration) {
        this.duration = duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public void startCooldown(Player player) {
        startCooldown(player.getUniqueId());
    }
    
    public void startCooldown(UUID id) {
        startTimes.put(id, System.currentTimeMillis());
    }
    
    public boolean isCoolingDown(Player player) {
        return isCoolingDown(player.getUniqueId());
    }
    
    public boolean isCoolingDown(UUID id) {
        if (startTimes.containsKey(id)) {
            return System.currentTimeMillis() - duration >= startTimes.get(id);
        } else {
            return false;
        }
    }
}
