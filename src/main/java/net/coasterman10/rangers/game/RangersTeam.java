package net.coasterman10.rangers.game;

import org.bukkit.ChatColor;

public enum RangersTeam {
    RANGERS(ChatColor.GREEN), BANDITS(ChatColor.RED);
    
    static {
        RANGERS.opponent = BANDITS;
        BANDITS.opponent = RANGERS;
    }
    
    private RangersTeam opponent;
    private ChatColor chatColor;
    
    private RangersTeam(ChatColor chatColor) {
        this.chatColor = chatColor;
    }
    
    public RangersTeam opponent() {
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
