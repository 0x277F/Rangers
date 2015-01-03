package net.coasterman10.rangers.stats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.logging.Level;

import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.kits.ItemStackBuilder;
import net.minecraft.util.com.google.common.collect.BiMap;
import net.minecraft.util.com.google.common.collect.HashBiMap;
import net.minecraft.util.org.apache.commons.io.IOUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class StatManager {
    private static BiMap<UUID, Statistic> stats = HashBiMap.create();

    private static Rangers plugin;

    public static void initialize(Rangers r){
        plugin = r;
    }

    public static void load(UUID uuid, Statistic stat){
        stats.put(uuid, stat);
    }

    public static boolean loadFromFile(UUID uuid) throws IOException, InvalidConfigurationException {
        File f = new File(plugin.getDataFolder(), "stats/"+uuid.toString()+".yml");
        if(!f.exists())
            return false;
        else {
            FileConfiguration conf = new YamlConfiguration();
            conf.load(f);
            Statistic stat = new Statistic();
            for(String s : conf.getConfigurationSection("stats").getKeys(false)){
                Object value = conf.get("stats."+s);
                stat.set(s, value);
            }
            load(uuid, stat);
        }
        return true;
    }

    public static void saveToFile(UUID uuid, Statistic stat) throws IOException, InvalidConfigurationException {
        File f = new File(plugin.getDataFolder(), "stats/" + uuid.toString() + ".yml");
        if (!f.exists()) {
            try {
                InputStream orig = plugin.getResource("/stat.yml");
                FileOutputStream out = new FileOutputStream(f);
                IOUtils.copy(orig, out);
                orig.close();
                out.close();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create stat file for uuid "+uuid.toString()+"\n"+e.getMessage());
            }
        }
        FileConfiguration conf = new YamlConfiguration();
        conf.load(f);
        for(String s : stat.getMapCompound().keySet()){
            conf.set(s, stat.get(s));
        }
        conf.save(f);
    }

    public static Statistic getStatistic(UUID uuid){
        return stats.get(uuid);
    }

    public static boolean statExists(UUID uuid){
        return stats.containsKey(uuid);
    }

    public static void unloadStat(UUID uuid){
        stats.remove(uuid);
    }

    public static Inventory getStatGui(Player p){
        if(!statExists(p.getUniqueId()))
            return null;
        Statistic s = StatManager.getStatistic(p.getUniqueId());
        Inventory i = Bukkit.createInventory(p, 72, p.getDisplayName()+"'s Statistics");
        for(int n = 0; n<9; n++){
            i.setItem(n, new ItemStackBuilder(Material.STAINED_GLASS_PANE).setColor(DyeColor.GRAY).setDisplayName(ChatColor.BOLD + "General Stats").build());
        }
        i.setItem(10, new ItemStackBuilder(Material.GOLD_SWORD).setDisplayName("Kills").addLore("Melee Kills: "+s.get("meleeKills")).addLore("Bow Kills: "+s.get("bowKills")).addLore("Total Kills: "+s.get("kills")).build());
        i.setItem(12, new ItemStackBuilder(Material.WATCH).setDisplayName("Games").addLore("Total Games Played: "+s.get("games")).build());
        i.setItem(14, new ItemStackBuilder(Material.FIREWORK).setDisplayName("Wins/Losses").addLore("Total Games Won: " + s.get("wins")).addLore("Total Games Lost: " + s.get("losses")).build());
        i.setItem(16, new ItemStackBuilder(Material.IRON_BARDING).setDisplayName("Boss Status").addLore((Boolean)s.get("hasDonkey") ? "Has defeated Kalkara" : "Has yet to defeat Kalkara").build());
        for(int n = 27; n<36; n++){
            i.setItem(n, new ItemStackBuilder(Material.STAINED_GLASS_PANE).setColor(DyeColor.GREEN).setDisplayName(ChatColor.BOLD + "Ranger Stats").build());
        }
        i.setItem(36, new ItemStackBuilder(Material.LEATHER_HELMET).setColor(DyeColor.GREEN).setDisplayName("Kills").addLore("Ranger Bow Kills: " + s.get("rangerBowKills")).addLore("Ranger Melee Kills: " + s.get("rangerMeleeKills")).addLore("Total Kills as Ranger: " + s.get("rangerKills")).addLore("Bandit Leader Kills: " + s.get("banditLeaderKills")).build());
        i.setItem(38, new ItemStackBuilder(Material.FIREWORK).setDisplayName("Wins/Losses").addLore("Wins as Rangers: "+s.get("rangerWins")).addLore("Losses as Rangers: "+s.get("rangerLosses")).build());
        for(int n = 45; n<54; n++){
            i.setItem(n, new ItemStackBuilder(Material.STAINED_GLASS_PANE).setColor(DyeColor.RED).setDisplayName(ChatColor.BOLD + "Bandit Stats").build());
        }
        i.setItem(55, new ItemStackBuilder(Material.DIAMOND_SWORD).setDisplayName("Kills").addLore("Bandit Bow Kills: "+s.get("banditBowKills")).addLore("Bandit Melee Kills: "+s.get("banditMeleeKills")).addLore("Total Bandit Kills: "+s.get("banditKills")).build());
        i.setItem(57, new ItemStackBuilder(Material.GOLD_HELMET).setDisplayName("Bandit Leader").addLore("Games as Bandit Leader: " + s.get("banditLeaderTimes")).addLore("Kills as Bandit Leader: " + s.get("killsAsBanditLeader")).build());
        i.setItem(59, new ItemStackBuilder(Material.FIREWORK).setDisplayName("Wins/Losses").addLore("Wins as Bandits: "+s.get("banditWins")).addLore("Losses as Bandits: "+s.get("banditLosses")).build());
        return i;
    }

    public static void showStatGui(Player subject, Player showTo){
        Inventory i = getStatGui(subject);
        if(i != null)
            showTo.openInventory(i);
    }

    public static void update(UUID uuid, String stat, Object value) {
        Statistic s = stats.get(uuid);
        s.set(stat, value);
        stats.put(uuid, s);
    }
}
