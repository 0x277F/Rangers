package net.coasterman10.rangers.arena;

import java.util.ArrayList;
import java.util.Collection;

import me.confuser.barapi.BarAPI;
import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.game.GameState;
import net.coasterman10.rangers.game.GameStateTasks;
import net.coasterman10.rangers.game.RangersTeam;
import net.coasterman10.rangers.kits.Kit;
import net.coasterman10.rangers.player.RangersPlayer;
import net.coasterman10.rangers.player.RangersPlayer.PlayerState;
import net.coasterman10.rangers.player.RangersPlayer.PlayerType;
import net.coasterman10.rangers.util.FileConfigAccessor;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

public class BossfightArena extends Arena {
    private final Rangers plugin;
    private LivingEntity boss;

    public BossfightArena(String name, FileConfigAccessor config, Rangers plugin) {
        super(name, config, plugin);
        this.plugin = plugin;
        
        registerStateTasks(GameState.STARTING, new StartingState());
        registerStateTasks(GameState.RUNNING, new RunningState());
        registerStateTasks(GameState.ENDING, new EndingState());
    }

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }
        for (RangersTeam team : RangersTeam.values()) {
            if (spawns.get(team) == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ArenaType getType() {
        return ArenaType.BOSSFIGHT;
    }

    @Override
    public void onPlayerJoin(RangersPlayer player) {
        if (players.size() >= getMinPlayers()) {
            setState(GameState.STARTING);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        RangersPlayer player = RangersPlayer.getPlayer(e.getEntity());
        if (player.getArena() == this) {
            plugin.sendToLobby(player.getBukkitPlayer());
            boss.remove();
        }
    }
    
    private void startGame() {
        Location bossSpawn = spawns.get(RangersTeam.BANDITS);
        boss = (LivingEntity) bossSpawn.getWorld().spawnEntity(bossSpawn, EntityType.IRON_GOLEM); // TODO Spawn Kalkara
        for (RangersPlayer player : players) {
            player.resetPlayer();
            player.teleport(spawns.get(RangersTeam.RANGERS));
            Kit.RANGER.apply(player);
            player.setState(PlayerState.GAME_PLAYING);
            player.setType(PlayerType.RANGER);
            player.setTeam(RangersTeam.RANGERS);
        }
    }

    protected class StartingState implements GameStateTasks {
        private int countdownDuration;

        @Override
        public void start() {
            countdownDuration = getConfig().getInt("countdown-duration");
            seconds = countdownDuration;
            onSecond();
        }

        @Override
        public void onSecond() {
            if (seconds == 0) {
                setState(GameState.RUNNING);
            } else {
                for (RangersPlayer player : players) {
                    BarAPI.setMessage(player.getBukkitPlayer(), ChatColor.GREEN + "Starting in " + seconds, seconds
                            / (float) countdownDuration * 100F);
                }
                seconds--;
            }
        }
    }

    protected class RunningState implements GameStateTasks {
        @Override
        public void start() {
            startGame();
            onSecond();
        }

        @Override
        public void onSecond() {
            for (RangersPlayer player : players) {
                if (boss != null) {
                    if (!boss.isDead()) {
                        BarAPI.setMessage(player.getBukkitPlayer(), ChatColor.RED + "Kalkara",
                                (float) (boss.getHealth() / boss.getMaxHealth()) * 100F);
                    } else {
                        setState(GameState.ENDING);
                    }
                }
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

                // Cannot directly iterate or we will throw a CME
                Collection<RangersPlayer> allPlayers = new ArrayList<>(players);
                for (RangersPlayer player : allPlayers) {
                    plugin.sendToLobby(player.getBukkitPlayer());
                }
            } else {
                for (RangersPlayer player : players) {
                    BarAPI.setMessage(player.getBukkitPlayer(), ChatColor.GREEN + "Kalkara has been defeated!", 100F);
                }
                seconds--;
            }
        }
    }
}
