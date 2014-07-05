package net.coasterman10.rangers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class GameScoreboard {
    private final Scoreboard rangerBoard;
    private final Scoreboard banditBoard;

    // This class is a mess and I really didn't like writing it at all, but it's a necessity to have two scoreboards
    // so the players names' are colored by enemy/friendly which is dependent on the team.

    // TODO Use Maps instead of holding discrete variables

    public GameScoreboard() {
        rangerBoard = Bukkit.getScoreboardManager().getNewScoreboard();
        banditBoard = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective o;
        o = rangerBoard.registerNewObjective("obj", "dummy");
        o.setDisplayName(ChatColor.GOLD + "Heads Collected");
        o.setDisplaySlot(DisplaySlot.SIDEBAR);

        o = banditBoard.registerNewObjective("obj", "dummy");
        o.setDisplayName(ChatColor.GOLD + "Heads Collected");
        o.setDisplaySlot(DisplaySlot.SIDEBAR);

        Team t;
        t = rangerBoard.registerNewTeam("RANGERS");
        t.setAllowFriendlyFire(false);
        t.setPrefix(ChatColor.GREEN.toString());
        t.setCanSeeFriendlyInvisibles(true);
        t = rangerBoard.registerNewTeam("BANDITS");
        t.setPrefix(ChatColor.RED.toString());
        t = rangerBoard.registerNewTeam("BANDIT_LEADER");
        t.setPrefix(ChatColor.RED + "♛ ");
        t.setSuffix(" ♛");

        t = banditBoard.registerNewTeam("RANGERS");
        t.setPrefix(ChatColor.RED.toString());
        t = banditBoard.registerNewTeam("BANDITS");
        t.setAllowFriendlyFire(false);
        t.setPrefix(ChatColor.GREEN.toString());
        t.setCanSeeFriendlyInvisibles(true);
        t = banditBoard.registerNewTeam("BANDIT_LEADER");
        t.setPrefix(ChatColor.GREEN + "♛ ");
        t.setSuffix(" ♛");
    }

    public Scoreboard getScoreboard(GameTeam team) {
        switch (team) {
        case RANGERS:
            return rangerBoard;
        case BANDITS:
            return banditBoard;
        default:
            return null;
        }
    }

    public void setTeam(Player player, GameTeam team) {
        if (team == null) {
            for (GameTeam t : GameTeam.values()) {
                rangerBoard.getTeam(t.name()).removePlayer(player);
                banditBoard.getTeam(t.name()).removePlayer(player);
            }
            rangerBoard.getTeam("BANDIT_LEADER").removePlayer(player);
            banditBoard.getTeam("BANDIT_LEADER").removePlayer(player);
            return;
        }
        rangerBoard.getTeam(team.name()).addPlayer(player);
        rangerBoard.getTeam(team.opponent().name()).removePlayer(player);
        rangerBoard.getTeam("BANDIT_LEADER").removePlayer(player);
        banditBoard.getTeam(team.name()).addPlayer(player);
        banditBoard.getTeam(team.opponent().name()).removePlayer(player);
        rangerBoard.getTeam("BANDIT_LEADER").removePlayer(player);
    }

    public void setBanditLeader(Player player) {
        for (OfflinePlayer p : rangerBoard.getTeam("BANDIT_LEADER").getPlayers())
            rangerBoard.getTeam("BANDIT_LEADER").removePlayer(p);
        for (OfflinePlayer p : banditBoard.getTeam("BANDIT_LEADER").getPlayers())
            banditBoard.getTeam("BANDIT_LEADER").removePlayer(p);
        rangerBoard.getTeam("BANDIT_LEADER").addPlayer(player);
        banditBoard.getTeam("BANDIT_LEADER").addPlayer(player);
    }

    public void setScore(String entry, int score) {
        if (score == 0)
            setScore(entry, 1); // This ensures that the score of 0 is put onto the scoreboard
        rangerBoard.getObjective(DisplaySlot.SIDEBAR).getScore(entry).setScore(score);
        banditBoard.getObjective(DisplaySlot.SIDEBAR).getScore(entry).setScore(score);
    }

    public int getScore(String entry) {
        return rangerBoard.getObjective(DisplaySlot.SIDEBAR).getScore(entry).getScore();
    }
}
