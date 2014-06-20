package net.coasterman10.rangers;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class Game {
	public static final int MIN_PLAYERS = 4;
	public static final int MAX_PLAYERS = 10;

	private static int nextId;

	private final int id;
	private final GameSign sign;
	
	private Scoreboard scoreboard;
	private Objective kills;

	private Location lobbySpawn;
	private Location banditSpawn;
	private Location rangerSpawn;

	// If you are going to give me hell about using 3 collections, please stop using your grandmother's 90s PC
	private Collection<UUID> players = new HashSet<>();
	private Collection<UUID> bandits = new HashSet<>();
	private Collection<UUID> rangers = new HashSet<>();
	private UUID banditLeader;

	public Game(GameSign sign) {
		id = nextId++;

		Validate.notNull(sign);
		this.sign = sign;
		sign.setGame(this);
		
		sign.setPlayers(0);
		sign.setMapName("TODO: Map name");
		sign.setStatusMessage("In Lobby");
	}

	public int getId() {
		return id;
	}

	public boolean addPlayer(UUID id) {
		if (players.size() == MAX_PLAYERS)
			return false;
		players.add(id);
		Player p = Bukkit.getPlayer(id);
		p.teleport(lobbySpawn);
		p.setHealth(20.0);
		p.setFoodLevel(20);
		p.setSaturation(20F);
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		broadcast(ChatColor.YELLOW + p.getName() + ChatColor.AQUA + " joined the game");
		return true;
	}
	
	private void broadcast(String msg) {
		for (UUID id : players) {
			Bukkit.getPlayer(id).sendMessage(msg);
		}
	}
}
