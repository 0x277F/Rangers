package net.coasterman10.rangers.arena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.confuser.barapi.BarAPI;
import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.PlayerUtil;
import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.config.ConfigUtil;
import net.coasterman10.rangers.config.FileConfigAccessor;
import net.coasterman10.rangers.game.GamePlayer;
import net.coasterman10.rangers.game.GameScoreboard;
import net.coasterman10.rangers.game.GameTeam;
import net.coasterman10.rangers.kits.Kit;
import net.coasterman10.spectate.SpectateAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

public class ClassicArena extends Arena {
    private Map<GameTeam, Location> chests = new EnumMap<>(GameTeam.class);
    private Map<GameTeam, Collection<String>> headsToRedeem = new EnumMap<>(GameTeam.class);
    protected Map<GameTeam, Collection<GamePlayer>> teams = new EnumMap<>(GameTeam.class);
    protected GamePlayer banditLeader;
    private GameScoreboard scoreboard = new GameScoreboard();
    private EndingType ending = null;
    
    public ClassicArena(String name, FileConfigAccessor config, Plugin plugin) {
        super(name, config, plugin);

        stateTasks.put(GameState.LOBBY, new LobbyState());
        stateTasks.put(GameState.STARTING, new StartingState());
        stateTasks.put(GameState.RUNNING, new RunningState());
        stateTasks.put(GameState.ENDING, new EndingState());

        for (GameTeam team : GameTeam.values()) {
            teams.put(team, new HashSet<GamePlayer>());
            headsToRedeem.put(team, new HashSet<String>());
        }
    }
    
    @Override
    public void load() {
        super.load();
        for (GameTeam team : GameTeam.values()) {
            chests.put(team, ConfigUtil.getLocation(getConfig(), "chests." + team.name().toLowerCase()));
        }
    }
    
    @Override
    public void save() {
        for (GameTeam team : GameTeam.values()) {
            ConfigUtil.setLocation(getConfig(), "chests." + team.name().toLowerCase(), chests.get(team));
        }
        super.save();
    }
    
    public void setChest(GameTeam team, Location chest) {
        chests.put(team, chest);
    }
    
    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }
        for (GameTeam team : GameTeam.values()) {
            if (spawns.get(team) == null || chests.get(team) == null) {
                return false;
            }
        }
        return true;
    }

    protected void reset() {
        for (Collection<GamePlayer> team : teams.values()) {
            team.clear();
        }
        for (GamePlayer player : players) {
            BarAPI.setMessage(player.getHandle(), Rangers.instance().getBarMessage(), 100F);
            if (player.isAlive())
                player.getHandle().teleport(lobbySpawn);
            PlayerUtil.resetPlayer(player.getHandle());
            player.setCanDoubleJump(false);
            player.setTeam(null);
            player.setAlive(false);
        }
        scoreboard.reset();
        banditLeader = null;

        if (state == GameState.LOBBY && players.size() >= getConfig().getInt("min-players")) {
            setState(GameState.STARTING);
        }
    }

    protected void start() {
        scoreboard.setScore(GameTeam.RANGERS, 0);
        scoreboard.setScore(GameTeam.BANDITS, 0);

        clearEntities();

        List<GamePlayer> bandits = new ArrayList<>(teams.get(GameTeam.BANDITS));
        banditLeader = bandits.get(new Random().nextInt(bandits.size()));
        scoreboard.setBanditLeader(banditLeader.getHandle());
        broadcast(ChatColor.RED + banditLeader.getName() + " is the Bandit Leader");

        for (GamePlayer player : players) {
            SpectateAPI.removeSpectator(player.getHandle());
            PlayerUtil.resetPlayer(player.getHandle());
            player.getHandle().teleport(spawns.get(player.getTeam()));
            player.setAlive(true);

            // UGLY HACK - If the map is named Moon, Jump Boost II, Weakness I, and Mining Fatigue I will be added
            if (getName().equals("Moon")) {
                PlayerUtil.addPermanentEffect(player.getHandle(), PotionEffectType.JUMP, 1);
                PlayerUtil.addPermanentEffect(player.getHandle(), PotionEffectType.WEAKNESS, 0);
                PlayerUtil.addPermanentEffect(player.getHandle(), PotionEffectType.SLOW_DIGGING, 0);
            }
        }

        for (GamePlayer ranger : teams.get(GameTeam.RANGERS)) {
            Kit.RANGER.apply(ranger);
            ranger.setCanDoubleJump(true);
            PlayerUtil.addPermanentEffect(ranger.getHandle(), PotionEffectType.DAMAGE_RESISTANCE, 0);
            PlayerUtil.addPermanentEffect(ranger.getHandle(), PotionEffectType.SPEED, 0);
            headsToRedeem.get(GameTeam.RANGERS).add(ranger.getHandle().getName());
        }

        // If rangers and bandits are unbalanced, do not give bandits slowness
        boolean slowness = teams.get(GameTeam.RANGERS).size() != teams.get(GameTeam.BANDITS).size()
                || teams.get(GameTeam.BANDITS).size() > 2;
        for (GamePlayer bandit : teams.get(GameTeam.BANDITS)) {
            Kit.BANDIT.apply(bandit);
            PlayerUtil.addPermanentEffect(bandit.getHandle(), PotionEffectType.DAMAGE_RESISTANCE, 0);
            if (slowness) {
                PlayerUtil.addPermanentEffect(bandit.getHandle(), PotionEffectType.SLOW, 0);
            }
        }

        headsToRedeem.get(GameTeam.BANDITS).add(banditLeader.getName());
    }

    private void selectTeams() {
        LinkedList<GamePlayer> playersToAdd = new LinkedList<>(players);
        Collections.shuffle(playersToAdd);
        GameTeam nextTeam = GameTeam.RANGERS;
        while (!playersToAdd.isEmpty()) {
            GamePlayer next = playersToAdd.poll();
            teams.get(nextTeam).add(next);
            next.setTeam(nextTeam);
            next.sendMessage(ChatColor.DARK_AQUA + "You have been selected to join the " + nextTeam.getChatColor()
                    + nextTeam.getName());
            nextTeam = nextTeam.opponent();
        }
    }

    public GamePlayer getBanditLeader() {
        return banditLeader;
    }

    @Override
    protected void onPlayerJoin(GamePlayer player) {
        scoreboard.setForPlayer(player.getHandle());
    }

    @Override
    protected void onPlayerLeave(GamePlayer player) {
        player.getHandle().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        if (state == GameState.RUNNING) {
            
        }
    }

    @Override
    public ArenaType getType() {
        return ArenaType.CLASSIC;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHeadDeposit(PlayerInteractEvent e) {
        // Preconditions: Player must be holding an item, clicking on a chest, and part of this game.
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.SKULL_ITEM)
            return;
        Block clicked = e.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.CHEST)
            return;
        GamePlayer player = PlayerManager.getPlayer(e.getPlayer());
        if (player.getArena() != this)
            return;
        // Check if the chest they clicked on is the one corresponding to their team.
        GameTeam team = player.getTeam();
        GameTeam opponent = team.opponent();
        if (clicked.getLocation().equals(chests.get(team))) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta.hasOwner()) {
                // If this head is expected to be redeemed, score the point and delete the head.
                if (headsToRedeem.get(opponent).remove(meta.getOwner())) {
                    scoreboard.incrementScore(opponent);
                    e.getPlayer().setItemInHand(null);
                    e.setCancelled(true);
                    // Check for victory
                    if (headsToRedeem.get(opponent).isEmpty()) {
                        switch (opponent) {
                        case RANGERS:
                            broadcast(ChatColor.RED + "The Rangers have been defeated!");
                            broadcast(ChatColor.GREEN + "The Bandits win!");
                            ending = EndingType.BANDITS_WIN;
                            break;
                        case BANDITS:
                            broadcast(ChatColor.RED + "The Bandits have been defeated!");
                            broadcast(ChatColor.GREEN + "The Rangers win!");
                            ending = EndingType.RANGERS_WIN;
                            break;
                        }
                        setState(GameState.ENDING);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        // Precondition: player must be participating in this game
        GamePlayer player = PlayerManager.getPlayer(e.getEntity());
        if (!players.contains(player))
            return;

        // End the game if all players have died
        boolean playersAlive = false;
        for (GamePlayer p : players) {
            if (p.isAlive()) {
                playersAlive = true;
                break;
            }
        }
        if (!playersAlive) {
            ending = EndingType.ALL_DIED;
            setState(GameState.ENDING);
        }
    }

    protected class LobbyState implements GameStateTasks {
        @Override
        public void start() {
            reset();
            if (players.size() >= getConfig().getInt("min-players")) {
                setState(GameState.STARTING);
            }
        }

        @Override
        public void onSecond() {

        }
    }

    protected class StartingState implements GameStateTasks {
        @Override
        public void start() {
            seconds = getConfig().getInt("countdown-duration");
            onSecond();
        }

        @Override
        public void onSecond() {
            if (seconds == 0) {
                setState(GameState.RUNNING);
            } else {
                if (seconds == 10) {
                    selectTeams();
                }
                for (GamePlayer player : players) {
                    BarAPI.setMessage(player.getHandle(), ChatColor.GREEN + "Starting in " + seconds, seconds
                            / (float) getConfig().getInt("countdown-duration") * 100F);
                }
                seconds--;
            }
        }
    }

    protected class RunningState implements GameStateTasks {
        @Override
        public void start() {
            start();
            seconds = getConfig().getInt("time-limit");
            onSecond();
        }

        @Override
        public void onSecond() {
            if (seconds == 0) {
                if (ending == null)
                    ending = EndingType.TIME;
                setState(GameState.ENDING);
            } else {
                for (GamePlayer player : players) {
                    BarAPI.setMessage(player.getHandle(), (seconds >= 30 ? ChatColor.GREEN : ChatColor.RED).toString()
                            + (seconds / 60) + (seconds % 60 >= 10 ? ":" : ":0") + (seconds % 60), seconds
                            / (float) getConfig().getInt("time-limit") * 100F);
                }
            }
            seconds--;
        }
    }

    protected class EndingState implements GameStateTasks {
        @Override
        public void start() {
            seconds = getConfig().getInt("restart-delay");
            onSecond();
        }

        @Override
        public void onSecond() {
            if (seconds == 0) {
                setState(GameState.LOBBY);
                ending = null;
            } else {
                for (GamePlayer player : players) {
                    BarAPI.setMessage(player.getHandle(), ending.message, 100F);
                }
            }
            seconds--;
        }
    }

    protected enum EndingType {
        ALL_DIED(ChatColor.GOLD + "Mutual Death - Draw"), TIME(ChatColor.GOLD + "Time Expired - Draw"), RANGERS_WIN(
                ChatColor.GREEN + "Bandit Leader defeated - Rangers Win"), BANDITS_WIN(ChatColor.GREEN
                + "Rangers defeated - Bandits Win");

        public final String message;

        private EndingType(String message) {
            this.message = message;
        }
    }
}
