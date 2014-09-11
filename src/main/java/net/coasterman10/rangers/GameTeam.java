package net.coasterman10.rangers;

import org.bukkit.ChatColor;

public enum GameTeam {
    RANGERS(ChatColor.GREEN), BANDITS(ChatColor.RED), SPECTATORS(ChatColor.DARK_AQUA);
    
    static {
        RANGERS.opponent = BANDITS;
        BANDITS.opponent = RANGERS;
    }
    
    private GameTeam opponent;
    private ChatColor chatColor;
    
    private GameTeam(ChatColor chatColor) {
        this.chatColor = chatColor;
    }
    
    public GameTeam opponent() {
        return opponent;
    }

    public String getName() {
        // First letter is capitalized, rest is lowercase
        return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
    }

    public ChatColor getChatColor() {
        return chatColor;
    }
}
