package net.coasterman10.rangers;

public enum GameTeam {
    RANGERS, BANDITS;
    
    public GameTeam opponent() {
        switch (this) {
        case RANGERS:
            return BANDITS;
        case BANDITS:
            return RANGERS;
        }
        return null; // Shut the hell up Eclipse
    }

    public String getName() {
        return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
    }
}
