package net.coasterman10.rangers.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.confuser.barapi.BarAPI;
import net.coasterman10.rangers.PlayerManager;
import net.coasterman10.rangers.PlayerUtil;
import net.coasterman10.rangers.arena.ClassicArena;
import net.coasterman10.rangers.kits.Kit;
import net.coasterman10.spectate.SpectateAPI;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

public class ClassicGame extends Game {
    private static final Random rand = new Random();

    private Map<GameTeam, Collection<GamePlayer>> teams = new EnumMap<>(GameTeam.class);
    private Map<GameTeam, Collection<String>> headsToRedeem = new EnumMap<>(GameTeam.class);
    private GamePlayer banditLeader;

    private GameScoreboard scoreboard;

    public ClassicGame(GameSettings settings, ClassicArena arena, Plugin plugin) {
        super(settings, arena, plugin);

        for (GameTeam team : GameTeam.values()) {
            teams.put(team, new HashSet<GamePlayer>());
            headsToRedeem.put(team, new HashSet<String>());
        }

        scoreboard = new GameScoreboard();

        stateTasks.put(State.STARTING, new Runnable() {
            @Override
            public void run() {
                if (seconds == 0) {
                    start();
                } else {
                    if (seconds == ClassicGame.this.settings.teamSelectTime) {
                        selectTeams();
                    }
                    for (GamePlayer p : players) {
                        float percent = seconds / (float) ClassicGame.this.settings.countdownDuration * 100F;
                        BarAPI.setMessage(p.getHandle(), ChatColor.GREEN + "Starting in " + seconds, percent);
                    }
                    seconds--;
                }
            }
        });
        stateTasks.put(State.RUNNING, new Runnable() {
            @Override
            public void run() {
                onSecond();
            }
        });
        stateTasks.put(State.ENDING, new Runnable() {
            @Override
            public void run() {
                if (seconds == 0) {
                    if (players.size() >= ClassicGame.this.settings.minPlayers) {
                        state = State.STARTING;
                    } else {
                        state = State.LOBBY;
                    }
                } else {
                    if (seconds == ClassicGame.this.settings.teamSelectTime) {
                        selectTeams();
                    }
                    for (GamePlayer p : players) {
                        float percent = seconds / (float) ClassicGame.this.settings.countdownDuration * 100F;
                        BarAPI.setMessage(p.getHandle(), ChatColor.GREEN + "Restarting in " + seconds, percent);
                    }
                    seconds--;
                }
            }
        });
    }

    public void removePlayer(GamePlayer player) {
        for (Collection<GamePlayer> team : teams.values())
            team.remove(player);
    }

    @Override
    protected void reset() {
        for (Collection<GamePlayer> team : teams.values()) {
            team.clear();
        }
        for (GamePlayer p : players) {
            BarAPI.setMessage(p.getHandle(), settings.idleBarMessage, 100F);
            if (p.isAlive())
                arena.sendToLobby(p);
            PlayerUtil.resetPlayer(p.getHandle());
            p.setCanDoubleJump(false);
            p.setTeam(null);
            p.setAlive(false);
        }
        scoreboard.reset();
        banditLeader = null;
        state = State.LOBBY;

        if (state == State.LOBBY && players.size() >= settings.minPlayers) {
            state = State.STARTING;
            seconds = settings.countdownDuration;
        }
    }

    @Override
    protected void selectTeams() {
        GameTeam nextTeam = GameTeam.RANGERS;
        List<GamePlayer> playerList = new ArrayList<>(players);
        Collections.shuffle(playerList);
        for (GamePlayer p : playerList) {
            p.setTeam(nextTeam);
            scoreboard.setTeam(p.getHandle(), nextTeam);
            teams.get(nextTeam).add(p);
            p.getHandle().sendMessage(
                    ChatColor.AQUA + "You have been selected to join the " + ChatColor.YELLOW + nextTeam.name());
            nextTeam = nextTeam.opponent();
        }
    }

    @Override
    protected void start() {
        seconds = settings.timeLimit;
        scoreboard.setScore(GameTeam.RANGERS, 0);
        scoreboard.setScore(GameTeam.BANDITS, 0);

        arena.clearGround();

        List<GamePlayer> bandits = new ArrayList<>(teams.get(GameTeam.BANDITS));
        banditLeader = bandits.get(rand.nextInt(bandits.size()));
        scoreboard.setBanditLeader(banditLeader.getHandle());
        broadcast(ChatColor.YELLOW + banditLeader.getName() + ChatColor.AQUA + " is the " + ChatColor.RED
                + "Bandit Leader");

        for (GamePlayer p : players) {
            SpectateAPI.removeSpectator(p.getHandle());
            PlayerUtil.resetPlayer(p.getHandle());
            arena.sendToGame(p);
            p.setAlive(true);

            // UGLY HACK - If the map is named Moon, Jump Boost II, Weakness I, and Mining Fatigue I will be added
            if (arena.getName().equals("Moon")) {
                PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.JUMP, 1);
                PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.WEAKNESS, 0);
                PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.SLOW_DIGGING, 0);
            }
        }

        for (GamePlayer p : teams.get(GameTeam.RANGERS)) {
            Kit.RANGER.apply(p);
            p.setCanDoubleJump(true);
            PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.DAMAGE_RESISTANCE, 0);
            PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.SPEED, 0);
            headsToRedeem.get(GameTeam.RANGERS).add(p.getHandle().getName());
        }

        // If rangers and bandits are unbalanced, do not give bandits slowness
        boolean slowness = teams.get(GameTeam.RANGERS).size() == teams.get(GameTeam.BANDITS).size()
                && teams.get(GameTeam.BANDITS).size() < 2;
        for (GamePlayer p : teams.get(GameTeam.BANDITS)) {
            Kit.BANDIT.apply(p);
            PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.DAMAGE_RESISTANCE, 0);
            if (slowness)
                PlayerUtil.addPermanentEffect(p.getHandle(), PotionEffectType.SLOW, 0);
        }

        headsToRedeem.get(GameTeam.BANDITS).add(banditLeader.getName());
       
        state = State.RUNNING;
    }

    @Override
    protected void onSecond() {
        if (seconds == 0) {
            broadcast(ChatColor.RED + "Time has expired!");
            broadcast(ChatColor.GOLD + "The game is a draw.");
            state = State.ENDING;
        } else {
            for (GamePlayer player : players) {
                int m = seconds / 60; // Minutes
                int s = seconds % 60; // Seconds
                float percent = (float) seconds / (float) settings.timeLimit * 100F;
                BarAPI.setMessage(player.getHandle(), (seconds < 30 ? ChatColor.RED : ChatColor.GREEN).toString() + m
                        + ":" + (s < 10 ? "0" + s : s) + " remaining", percent);
            }

            seconds--;
        }
    }

    @EventHandler
    public void onHeadDeposit(PlayerInteractEvent e) {
        // Preconditions: Player must be holding an item, clicking on a chest, and part of this game.
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.SKULL_ITEM)
            return;
        Block clicked = e.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.CHEST)
            return;
        GamePlayer player = PlayerManager.getPlayer(e.getPlayer());
        if (player.getGame().id != id)
            return;
        // Check if the chest they clicked on is the one corresponding to their team.
        GameTeam team = player.getTeam();
        GameTeam opponent = team.opponent();
        if (clicked.getLocation().equals(arena.getChest(team))) {
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
                            break;
                        case BANDITS:
                            broadcast(ChatColor.RED + "The Bandits have been defeated!");
                            broadcast(ChatColor.GREEN + "The Rangers win!");
                            break;
                        }
                        seconds = settings.restartDelay;
                        state = State.ENDING;
                    }
                }
            }
        }
    }
    
    public GamePlayer getBanditLeader() {
        return banditLeader;
    }
}
