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
import net.coasterman10.rangers.game.GameScoreboard;
import net.coasterman10.rangers.game.GameState;
import net.coasterman10.rangers.game.GameStateTasks;
import net.coasterman10.rangers.game.RangersTeam;
import net.coasterman10.rangers.kits.Kit;
import net.coasterman10.rangers.player.RangersPlayer;
import net.coasterman10.rangers.player.RangersPlayer.PlayerState;
import net.coasterman10.rangers.util.ConfigUtil;
import net.coasterman10.rangers.util.FileConfigAccessor;
import net.coasterman10.spectate.SpectateAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

public class ClassicArena extends Arena {
    private Map<RangersTeam, Location> chests = new EnumMap<>(RangersTeam.class);
    private Map<RangersTeam, Collection<String>> headsToRedeem = new EnumMap<>(RangersTeam.class);
    protected Map<RangersTeam, Collection<RangersPlayer>> teams = new EnumMap<>(RangersTeam.class);
    protected RangersPlayer banditLeader;
    private GameScoreboard scoreboard = new GameScoreboard();
    private EndingType ending = null;

    public ClassicArena(String name, FileConfigAccessor config, Plugin plugin) {
        super(name, config, plugin);

        registerStateTasks(GameState.LOBBY, new LobbyState());
        registerStateTasks(GameState.STARTING, new StartingState());
        registerStateTasks(GameState.RUNNING, new RunningState());
        registerStateTasks(GameState.ENDING, new EndingState());

        for (RangersTeam team : RangersTeam.values()) {
            teams.put(team, new HashSet<RangersPlayer>());
            headsToRedeem.put(team, new HashSet<String>());
        }
    }

    @Override
    public void load() {
        super.load();
        for (RangersTeam team : RangersTeam.values()) {
            chests.put(team, ConfigUtil.getLocation(getConfig(), "chests." + team.name().toLowerCase()));
        }
    }

    @Override
    public void save() {
        for (RangersTeam team : RangersTeam.values()) {
            ConfigUtil.setLocation(getConfig(), "chests." + team.name().toLowerCase(), chests.get(team));
        }
        super.save();
    }

    public void setChest(RangersTeam team, Location chest) {
        chests.put(team, chest);
    }

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }
        for (RangersTeam team : RangersTeam.values()) {
            if (spawns.get(team) == null || chests.get(team) == null) {
                return false;
            }
        }
        return true;
    }

    protected void reset() {
        for (Collection<RangersPlayer> team : teams.values()) {
            team.clear();
        }
        for (Collection<String> heads : headsToRedeem.values()) {
            heads.clear();
        }
        for (RangersPlayer player : players) {
            if (player.isPlaying())
                player.teleport(lobbySpawn);
            player.resetPlayer();
            player.setTeam(null);
            player.setState(PlayerState.GAME_LOBBY);
        }
        scoreboard.reset();
        banditLeader = null;

        if (getState() == GameState.LOBBY && players.size() >= getConfig().getInt("min-players")) {
            setState(GameState.STARTING);
        }
    }

    protected void startGame() {
        scoreboard.setScore(RangersTeam.RANGERS, 0);
        scoreboard.setScore(RangersTeam.BANDITS, 0);

        clearEntities();

        List<RangersPlayer> bandits = new ArrayList<>(teams.get(RangersTeam.BANDITS));
        banditLeader = bandits.get(new Random().nextInt(bandits.size()));
        scoreboard.setBanditLeader(banditLeader.getBukkitPlayer());
        broadcast(ChatColor.RED + banditLeader.getName() + " is the Bandit Leader");

        // Teleport all players to arena and set them as alive
        for (RangersPlayer player : players) {
            SpectateAPI.removeSpectator(player.getBukkitPlayer());
            player.resetPlayer();
            player.teleport(spawns.get(player.getTeam()));
            player.setState(PlayerState.GAME_PLAYING);
        }

        // Add permanent effects for arnea
        ConfigurationSection effects = getConfig().getConfigurationSection("effects");
        if (effects != null) {
            for (String effect : getConfig().getConfigurationSection("effects").getKeys(false)) {
                try {
                    @SuppressWarnings("deprecation")
                    PotionEffectType type = PotionEffectType.getById(Integer.parseInt(effect));
                    if (type != null) {
                        int amp = Math.max(0, effects.getInt(effect));
                        for (RangersPlayer player : players) {
                            player.addPermanentEffect(type, amp);
                        }
                    }
                } catch (NumberFormatException e) {
                }
            }
        }

        for (RangersPlayer ranger : teams.get(RangersTeam.RANGERS)) {
            Kit.RANGER.apply(ranger);
            ranger.addPermanentEffect(PotionEffectType.DAMAGE_RESISTANCE, 0);
            ranger.addPermanentEffect(PotionEffectType.SPEED, 0);
            headsToRedeem.get(RangersTeam.RANGERS).add(ranger.getName());
        }

        // If teams are unbalanced and there are 2 or fewer bandits, do not give bandits slowness
        boolean slowness = teams.get(RangersTeam.RANGERS).size() == teams.get(RangersTeam.BANDITS).size()
                || teams.get(RangersTeam.BANDITS).size() > 2;
        for (RangersPlayer bandit : teams.get(RangersTeam.BANDITS)) {
            Kit.BANDIT.apply(bandit);
            bandit.addPermanentEffect(PotionEffectType.DAMAGE_RESISTANCE, 0);
            if (slowness) {
                bandit.addPermanentEffect(PotionEffectType.SLOW, 0);
            } else {
                bandit.sendMessage(ChatColor.GOLD + "The teams are unbalanced, so you have been spared of slowness.");
            }
        }

        headsToRedeem.get(RangersTeam.BANDITS).add(banditLeader.getName());
    }

    private void selectTeams() {
        LinkedList<RangersPlayer> playersToAdd = new LinkedList<>(players);
        Collections.shuffle(playersToAdd);
        RangersTeam nextTeam = RangersTeam.RANGERS;
        while (!playersToAdd.isEmpty()) {
            RangersPlayer next = playersToAdd.poll();
            teams.get(nextTeam).add(next);
            next.setTeam(nextTeam);
            scoreboard.setTeam(next.getBukkitPlayer(), nextTeam);
            next.sendMessage(ChatColor.DARK_AQUA + "You have been selected to join the " + nextTeam.getChatColor()
                    + nextTeam.getName());
            nextTeam = nextTeam.opponent();
        }
    }

    public RangersPlayer getBanditLeader() {
        return banditLeader;
    }

    @Override
    protected void onPlayerJoin(RangersPlayer player) {
        scoreboard.setForPlayer(player.getBukkitPlayer());
        if (getState() == GameState.LOBBY && players.size() >= getMinPlayers()) {
            setState(GameState.STARTING);
        }
    }

    @Override
    protected void onPlayerLeave(RangersPlayer player) {
        player.getBukkitPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        if ((getState() == GameState.STARTING && players.size() < getMinPlayers()) || players.size() == 0) {
            setState(GameState.LOBBY);
        } else if (player.isPlaying()) {
            player.dropHead();
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
        RangersPlayer player = RangersPlayer.getPlayer(e.getPlayer());
        if (player.getArena() != this)
            return;
        // Check if the chest they clicked on is the one corresponding to their team.
        RangersTeam team = player.getTeam();
        RangersTeam opponent = team.opponent();
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
        RangersPlayer player = RangersPlayer.getPlayer(e.getEntity());
        if (!players.contains(player))
            return;

        // End the game if all players have died
        boolean playersPlaying = false;
        for (RangersPlayer p : players) {
            if (p.isPlaying()) {
                playersPlaying = true;
                break;
            }
        }
        if (!playersPlaying) {
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
        private int countdownDuration;
        private int teamSelect;

        @Override
        public void start() {
            countdownDuration = getConfig().getInt("countdown-duration");
            teamSelect = getConfig().getInt("team-select");
            seconds = countdownDuration;
            onSecond();
        }

        @Override
        public void onSecond() {
            if (seconds == 0) {
                setState(GameState.RUNNING);
            } else {
                if (seconds == teamSelect) {
                    selectTeams();
                }
                for (RangersPlayer player : players) {
                    BarAPI.setMessage(player.getBukkitPlayer(), ChatColor.GREEN + "Starting in " + seconds, seconds
                            / (float) countdownDuration * 100F);
                }
                seconds--;
            }
        }
    }

    protected class RunningState implements GameStateTasks {
        private int timeLimit;

        @Override
        public void start() {
            timeLimit = getConfig().getInt("time-limit");
            seconds = timeLimit;
            startGame();
            onSecond();
        }

        @Override
        public void onSecond() {
            if (seconds == 0) {
                if (ending == null)
                    ending = EndingType.TIME;
                setState(GameState.ENDING);
            } else {
                for (RangersPlayer player : players) {
                    BarAPI.setMessage(player.getBukkitPlayer(),
                            (seconds >= 30 ? ChatColor.GREEN : ChatColor.RED).toString() + (seconds / 60)
                                    + (seconds % 60 >= 10 ? ":" : ":0") + (seconds % 60), seconds / (float) timeLimit
                                    * 100F);
                }
                seconds--;
            }
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
                for (RangersPlayer player : players) {
                    BarAPI.setMessage(player.getBukkitPlayer(), ending.message, 100F);
                }
                seconds--;
            }
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
