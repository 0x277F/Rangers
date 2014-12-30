package net.coasterman10.rangers.kits;

import net.coasterman10.rangers.player.RangersPlayer;

public interface Kit {
    public static final Kit RANGER = new RangerKit();
    public static final Kit BANDIT = new BanditKit();
    
    public void apply(RangersPlayer player);
}
