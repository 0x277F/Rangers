package net.coasterman10.rangers;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
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

    public void setScore(String entry, int score) {
        if (score == 0)
            setScore(entry, 1); // This ensures that the score of 0 is put onto the scoreboard
        for (Scoreboard board : boards.values()) {
            board.getObjective(DisplaySlot.SIDEBAR).getScore(entry).setScore(score);
        }
    }

    public int getScore(String entry) {
        return boards.get(GameTeam.RANGERS).getObjective(DisplaySlot.SIDEBAR).getScore(entry).getScore();
    }
}
