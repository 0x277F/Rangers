package net.coasterman10.rangers.game;

import java.util.EnumMap;
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
    private Map<GameTeam, Scoreboard> boards = new EnumMap<>(GameTeam.class);

    public GameScoreboard() {
        for (GameTeam t : GameTeam.values()) {
            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            boards.put(t, board);

            Objective o = board.registerNewObjective("obj", "dummy");
            o.setDisplayName(ChatColor.GOLD + "Heads Collected");
            o.setDisplaySlot(DisplaySlot.SIDEBAR);

            Team friendly = board.registerNewTeam(t.name());
            Team enemy = board.registerNewTeam(t.opponent().name());
            Team banditLeader = board.registerNewTeam("BANDIT_LEADER");
            friendly.setPrefix(ChatColor.GREEN.toString());
            enemy.setPrefix(ChatColor.RED.toString());
            banditLeader.setPrefix((t == GameTeam.BANDITS ? ChatColor.GREEN : ChatColor.RED) + "♛ ");
            banditLeader.setSuffix(" ♛");
        }
    }

    public Scoreboard getScoreboard(GameTeam team) {
        return boards.get(team);
    }

    public void setTeam(Player player, GameTeam team) {
        for (Scoreboard board : boards.values()) {
            for (Team t : board.getTeams())
                t.removePlayer(player);
            if (team != null)
                board.getTeam(team.name()).addPlayer(player);
        }
    }

    public void setBanditLeader(Player player) {
        for (Scoreboard board : boards.values()) {
            for (OfflinePlayer p : board.getTeam("BANDIT_LEADER").getPlayers())
                board.getTeam("BANDIT_LEADER").removePlayer(p);
            board.getTeam("BANDIT_LEADER").addPlayer(player);
        }
    }

    public void setScore(GameTeam team, int score) {
        if (score == 0)
            setScore(team, 1); // This ensures that the score of 0 is put onto the scoreboard
        for (Scoreboard board : boards.values()) {
            board.getObjective(DisplaySlot.SIDEBAR).getScore(team.getName()).setScore(score);
        }
    }

    public int getScore(GameTeam team) {
        return boards.get(GameTeam.RANGERS).getObjective(DisplaySlot.SIDEBAR).getScore(team.getName()).getScore();
    }

    public void incrementScore(GameTeam team) {
        for (Scoreboard board : boards.values()) {
            Score s = board.getObjective(DisplaySlot.SIDEBAR).getScore(team.getName());
            s.setScore(s.getScore() + 1);
        }
    }

    public void reset() {
        for (GameTeam t : GameTeam.values()) {
            for (GameTeam tt : GameTeam.values()) {
                boards.get(t).resetScores(tt.getName());
            }
        }
    }
}
