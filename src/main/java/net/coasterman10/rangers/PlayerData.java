package net.coasterman10.rangers;

public class PlayerData {
    private Game game;
    private Team team;
    
    public void setTeam(Team team) {
        this.team = team;
    }
    
    public Team getTeam() {
        return team;
    }
    
    public void setGame(Game game) {
        this.game = game;
    }
    
    public Game getGame() {
        return game;
    }
}
