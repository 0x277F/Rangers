package net.coasterman10.rangers.kits;

import net.coasterman10.rangers.game.GamePlayer;

public interface Kit {
    public static final Kit RANGER = new RangerKit();
    public static final Kit BANDIT = new BanditKit();
    
    public void apply(GamePlayer player);
}
