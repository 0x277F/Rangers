package net.coasterman10.rangers.menu;

import net.coasterman10.rangers.util.SignText;

import org.bukkit.entity.Player;

public interface PreferenceMenu {
    public static final PreferenceMenu RANGER_ABILITY = new RangerAbilityMenu();
    public static final PreferenceMenu RANGER_SECONDARY = new RangerSecondaryMenu();
    public static final PreferenceMenu RANGER_BOW = new RangerBowMenu();
    public static final PreferenceMenu BANDIT_ABILITY = new BanditAbilityMenu();
    public static final PreferenceMenu BANDIT_SECONDARY = new BanditSecondaryMenu();
    public static final PreferenceMenu BANDIT_BOW = new BanditBowMenu();

    public static final PreferenceMenu[] menus = new PreferenceMenu[] { RANGER_ABILITY, RANGER_SECONDARY, RANGER_BOW,
            BANDIT_ABILITY, BANDIT_SECONDARY, BANDIT_BOW };

    public String getTitle();

    public SignText getSignText();

    public String getPreferenceKey();

    public void open(Player player);

    public void selectItem(Player player, int index);
}
