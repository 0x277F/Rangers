package net.coasterman10.rangers;

public class PlayerData {
    private Game game;
    private GameTeam team;
    private boolean alive;
    
    public void setTeam(GameTeam team) {
        this.team = team;
    }
    
    public GameTeam getTeam() {
        return team;
    }
    
    public void setGame(Game game) {
        this.game = game;
    }
    
    public Game getGame() {
        return game;
    }
    
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    public boolean isAlive() {
        return alive;
    }
}
