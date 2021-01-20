package samb.com.database;

import java.io.Serializable;

public class UserStats implements Serializable {
	// This class contains statistical data about a specific user (determined by the id)
	// A single row in the stats table will be 1 UserStats object
	
	private static final long serialVersionUID = 2791255168182465569L;
	public String id;
	public int elo, noGames, noGamesWon, noGamesLost, noBallsPotted, highestElo, highestEloVictory;
	
	public UserStats() {}
	
	public UserStats(String id) {
		this.id = id;
		this.elo = 1000;
		this.noGames = 0;
		this.noGamesWon = 0;
		this.noGamesLost = 0;
		this.noBallsPotted = 0;
		this.highestElo = 0;
		this.highestEloVictory = 0;
	}
	
	public UserStats(String id, int elo, int ng, int ngw, int ngl, int nbp, int he, int hev) {
		this.id = id;
		this.elo = elo;
		this.noGames = ng;
		this.noGamesWon = ngw;
		this.noGamesLost = ngl;
		this.noBallsPotted = nbp;
		this.highestElo = he;
		this.highestEloVictory = hev;
		
	}
	
	
	public int[] toArray() {
		return new int[] {elo, noGames, noGamesWon, noGamesLost, noBallsPotted, highestElo, highestEloVictory};
	}
}
