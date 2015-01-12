package net.coasterman10.rangers.arena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import net.coasterman10.rangers.Rangers;
import net.coasterman10.rangers.game.GameState;
import net.coasterman10.rangers.game.GameStateTasks;
import net.coasterman10.rangers.game.RangersTeam;
import net.coasterman10.rangers.kits.Kit;
import net.coasterman10.rangers.player.RangersPlayer;
import net.coasterman10.rangers.player.RangersPlayer.PlayerState;
import net.coasterman10.rangers.player.RangersPlayer.PlayerType;
import net.coasterman10.rangers.util.ConfigUtil;
import net.coasterman10.rangers.util.FileConfigAccessor;
import net.coasterman10.spectate.SpectateAPI;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffectType;

public class WarArena extends ClassicArena {
    private static final Random rand = new Random();

    private Location rangerLanding;
    private Collection<RangersPlayer> deployingRangers = new HashSet<>();

    public WarArena(String name, FileConfigAccessor config, Rangers plugin) {
        super(name, config, plugin);

        registerStateTasks(GameState.RUNNING, new WarRunningState());
    }

    @Override
    public void load() {
        super.load();
        rangerLanding = ConfigUtil.getLocation(getConfig(), "ranger-landing");
    }

    @Override
    public void save() {
        ConfigUtil.setLocation(getConfig(), "ranger-landing", rangerLanding);
        super.save();
    }

    @Override
    public ArenaType getType() {
        return ArenaType.WAR;
    }

    @Override
    public boolean isValid() {
        if (!super.isValid())
            return false;
        if (rangerLanding == null)
            return false;
        return true;
    }

    @Override
    protected void startGame() {
        scoreboard.setScore(RangersTeam.RANGERS, 0);
        scoreboard.setScore(RangersTeam.BANDITS, 0);

        clearEntities();

        List<RangersPlayer> bandits = new ArrayList<>(teams.get(RangersTeam.BANDITS));
        banditLeader = bandits.get(new Random().nextInt(bandits.size()));
        banditLeader.setType(PlayerType.BANDIT_LEADER);
        scoreboard.setBanditLeader(banditLeader.getBukkitPlayer());
        broadcast(ChatColor.RED + banditLeader.getName() + " is the Bandit Leader");

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

        // Teleport and initialize rangers only
        for (RangersPlayer ranger : teams.get(RangersTeam.RANGERS)) {
            SpectateAPI.removeSpectator(ranger.getBukkitPlayer());
            ranger.resetPlayer();

            // Random spawn angle within a circle of given radius
            Location spawn = spawns.get(RangersTeam.RANGERS).clone();
            double radius = rand.nextDouble() * getConfig().getDouble("ranger-spawn-radius");
            double angle = rand.nextDouble() * 2 * Math.PI;
            spawn.add(radius * Math.cos(angle), 0, radius * Math.sin(angle));
            ranger.teleport(spawns.get(RangersTeam.RANGERS));

            ranger.getBukkitPlayer().setAllowFlight(true);
            ranger.getBukkitPlayer().setExp(Float.intBitsToFloat(Float.floatToIntBits(1F) - 1));
            ranger.addPermanentEffect(PotionEffectType.DAMAGE_RESISTANCE, 0);
            ranger.addPermanentEffect(PotionEffectType.SPEED, 0);
            ranger.setState(PlayerState.GAME_PLAYING);
            headsToRedeem.get(RangersTeam.RANGERS).add(ranger.getName());
            deployingRangers.add(ranger);
        }

        headsToRedeem.get(RangersTeam.BANDITS).add(banditLeader.getName());

        // TODO load up chests with goodies
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRangerLand(EntityDamageEvent e) {
        // Preconditions: must be fall damge, must be a Player, must be in this arena, must be deploying
        if (e.getCause() != DamageCause.FALL)
            return;
        if (!(e.getEntity() instanceof Player))
            return;
        RangersPlayer player = RangersPlayer.getPlayer((Player) e.getEntity());
        if (player.getArena() != this)
            return;
        if (!deployingRangers.contains(player))
            return;

        Location loc = player.getBukkitPlayer().getLocation();
        loc.setY(rangerLanding.getY());
        if (loc.distance(rangerLanding) < getConfig().getInt("ranger-landing-radius")) {
            e.setCancelled(true);
            deployingRangers.remove(player);
            player.setCanDoubleJump(true);
        }
    }

    public class WarRunningState extends RunningState implements GameStateTasks {
        @Override
        public void start() {
            super.start();
        }

        @Override
        public void onSecond() {
            super.onSecond();
            if (seconds == getConfig().getInt("bandit-spawn-time")) {
                for (RangersPlayer bandit : teams.get(RangersTeam.BANDITS)) {
                    SpectateAPI.removeSpectator(bandit.getBukkitPlayer());
                    bandit.resetPlayer();
                    Kit.BANDIT.apply(bandit);
                    bandit.teleport(spawns.get(bandit.getTeam()));
                    bandit.addPermanentEffect(PotionEffectType.DAMAGE_RESISTANCE, 0);
                    bandit.addPermanentEffect(PotionEffectType.SLOW, 0);
                    bandit.setState(PlayerState.GAME_PLAYING);
                }
            }
        }
    }
}
