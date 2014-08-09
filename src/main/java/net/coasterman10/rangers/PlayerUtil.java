package net.coasterman10.rangers;

import org.bukkit.entity.Player;

public class PlayerUtil {
    private PlayerUtil() {
        
    }
    
    public static void enableDoubleJump(Player p) {
        // Smallest possible increment lower than 1.0 to make the bar appear full but not give a level
        p.setExp(Float.intBitsToFloat(Float.floatToIntBits(1F) - 1));
        p.setAllowFlight(true);
    }
    
    public static void disableDoubleJump(Player p) {
        p.setExp(0);
        p.setAllowFlight(false);
    }
}
