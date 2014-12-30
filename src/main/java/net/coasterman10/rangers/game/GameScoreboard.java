package net.coasterman10.rangers.game;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class GameScoreboard {
    private Scoreboard board;
    private final Map<RangersTeam, Team> teams = new EnumMap<>(RangersTeam.class);
    private final Map<RangersTeam, Score> scores = new EnumMap<>(RangersTeam.class);

    public GameScoreboard() {
        board = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective o = board.registerNewObjective("obj", "dummy");
        o.setDisplayName(ChatColor.GOLD + "Heads Collected");
        o.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (RangersTeam t : RangersTeam.values()) {
            Team team = board.registerNewTeam(t.name());
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setPrefix(t.getChatColor().toString());
            teams.put(t, team);

            scores.put(t, o.getScore(t.getName()));
            setScore(t, 0);
        }
    }

    public void setForPlayer(Player p) {
        p.setScoreboard(board);
    }

    public void setTeam(Player player, RangersTeam team) {
        if (board.getPlayerTeam(player) != null)
            board.getPlayerTeam(player).removePlayer(player);
        teams.get(team).addPlayer(player);
    }

    public void setBanditLeader(Player player) {
        // TODO
    }

    public void setScore(RangersTeam team, int score) {
        if (score == 0)
            setScore(team, 1); // This ensures that the score of 0 is put onto the scoreboard

        scores.get(team).setScore(score);
    }

    public int getScore(RangersTeam team) {
        return scores.get(team).getScore();
    }

    public void incrementScore(RangersTeam team) {
        setScore(team, getScore(team) + 1);
    }

    public void reset() {
        for (RangersTeam t : RangersTeam.values()) {
            clearTeam(teams.get(t));
            setScore(t, 0);
        }
    }

    private static void clearTeam(Team team) {
        for (Iterator<OfflinePlayer> it = team.getPlayers().iterator(); it.hasNext();) {
            team.removePlayer(it.next());
        }
    }
}
