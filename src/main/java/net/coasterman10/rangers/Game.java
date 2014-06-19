package net.coasterman10.rangers;

public class Game {
	public static final int MIN_PLAYERS = 4;
	public static final int MAX_PLAYERS = 10;
	
	private static int nextId;
	
	private final int id;
	
	public Game() {
		id = nextId++;
	}
	
	public int getId() {
		return id;
	}
}
