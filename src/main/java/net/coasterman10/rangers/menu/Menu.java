package net.coasterman10.rangers.menu;

import org.bukkit.entity.Player;

public interface Menu {
    public String getTitle();
    
    public void open(Player player);
    
    public void selectItem(Player player, int index);
}
