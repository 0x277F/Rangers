package net.coasterman10.rangers;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class GameSign {
	/** The block state corresponding to this sign */
	private Sign state;
	
	/** The game corresponding to this sign. Allows for easy access when a player clicks the sign. */
	private Game game;

	public GameSign(Block b) {
		Validate.notNull(b);
		if (!(b.getState() instanceof Sign))
			throw new IllegalArgumentException("Block " + b + " is not a sign");
		state = (Sign) b.getState();
	}

	public void setMapName(String mapName) {
		Validate.notNull(mapName);
		state.setLine(0, mapName);
		update();
	}

	public void setPlayers(int players) {
		String s = String.format("%s%d / %d", ChatColor.BOLD.toString(), players, Game.MAX_PLAYERS);
		if (players == Game.MAX_PLAYERS) {
			s = ChatColor.RED + s;
			state.setLine(3, ChatColor.RED + "Full");
		} else {
			state.setLine(3, ChatColor.GREEN + "Click to join");
		}
		state.setLine(1, s);
		update();
	}

	public void setStatusMessage(String statusMessage) {
		state.setLine(2, statusMessage);
		update();
	}

	public void update() {
		if (state.getBlock().getState() instanceof Sign) {
			// If the sign has been changed for any reason, update its facing direction and type
			// (in case someone has decided to turn the sign or change it from a post to a wall sign)
			org.bukkit.material.Sign data = (org.bukkit.material.Sign) state.getBlock().getState().getData();
			((org.bukkit.material.Sign) state.getData()).setFacingDirection(data.getFacing());
			state.setType(state.getBlock().getType());
			state.update();
		}
	}

	public void setGame(Game game) {
		this.game = game;
	}
	
	public Game getGame() {
		return game;
	}
}
