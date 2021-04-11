package samb.host.game;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import samb.client.utils.Maths;
import samb.com.database.UserInfo;
import samb.com.database.UserStats;
import samb.com.server.info.UpdateInfo;
import samb.com.server.info.Win;
import samb.com.server.packet.Error;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;
import samb.com.server.packet.UHeader;
import samb.com.utils.enums.TableUseCase;
import samb.host.database.StatsDBManager;
import samb.host.database.UserDBManager;
import samb.host.main.Host;

public class GameManager {
	/* This class handles game events, new games, updating games, spectating games, ending games, etc
	 * This class also handles who is playing / watching games
	 * The system to record this revolves around a Game ID ('gId') which links game objects and 'updators' to a game
	 * The updators map contains data revolving around who needs to be updated when a player does something in their game
	 * The parts map links a user to a game, key value pair being (key: user id, value: game id)
	 * */
	
	private HashMap<String, Game> games;                              // {gameID, Game Object}
	private HashMap<String, String> parts;                            // {uID, gameID}
	private HashMap<String, CopyOnWriteArrayList<String>> updators;   // {gameID, updators list}
	private HashMap<String, CopyOnWriteArrayList<String>> updateLink; // {uID, updators list}
	// The class 'CopyOnWriteArrayList' is a list which deals with any concurrency issues which may arise
	// (For example, if you remove an element from the list during an iteration of that list)
	// In a regular list, this would produce an error, but a COWAL deals with this abstractly
	
	public Pool pool;
	
	public GameManager() {
		this.games = new HashMap<>();
		this.parts = new HashMap<>();
		this.updators = new HashMap<>();
		this.updateLink = new HashMap<>();
		
		this.pool = new Pool(this);
	}
	
	public void newGame(String u1, String u2) {
		// This method creates a new game between user u1 and user u2, and any other necessary objects
		
		String id = UUID.randomUUID().toString();
		Game g = new Game(id, u1, u2);
		games.put(id, g);
		
		parts.put(u1, id);
		parts.put(u2, id);
		
		CopyOnWriteArrayList<String> cowal = new CopyOnWriteArrayList<>();
		updators.put(id, cowal);
		updators.get(id).add(u1);
		updators.get(id).add(u2);
		
		updateLink.put(u1, cowal);
		updateLink.put(u2, cowal);
		
		UserDBManager.setIngame(u1, true);
		UserDBManager.setIngame(u2, true);
		
		Packet p = new Packet(Header.newGame);
		p.gameInfo = g;
		p.gameInfo.tuc = TableUseCase.playing; // Only for the players
		p.gameState = g.state;
		
		Host.getHost().um.get(u1).send(p);
		
		p.gameInfo.first = false;
		Host.getHost().um.get(u2).send(p);
		
	}
	
	public void queueSpectate(String uToSpec, String spec) {
		// Since the game state must be requested from a player, the spectator must wait for them to reply
		Host.getHost().um.get(spec).waiting = true;
		
		Packet p = new Packet(Header.getUpdateGame);
		p.spec = spec;
		
		Host.getHost().um.get(uToSpec).send(p);
		
	}
	
	public void addSpectate(Packet up) {
		// Once a player has replied, the spectate can watch the match
		if(up.gameInfo == null) {
			System.out.println("Error occured when adding spectator");
			return;
		}
		
		updators.get(up.gameInfo.id).add(up.spec);
		updateLink.put(up.spec, updators.get(up.gameInfo.id));
		
		up.header = Header.spectate;
		up.gameInfo.tuc = TableUseCase.spectating; // Only for spectators
		
		Host.getHost().um.get(up.spec).waiting = false;
		Host.getHost().um.get(up.spec).send(up);
		
	}
	
	public void challenge(Packet p) {
		String opp = p.challengeInfo.oppId;
		if(!Host.getHost().um.isOnline(opp)) {
			p.challengeInfo.err = Error.notOnline;
			Host.getHost().um.get(p.id).send(p);
			return;
			
		} if(inGame(opp)) {
			p.challengeInfo.err = Error.alreadyInGame;
			Host.getHost().um.get(p.id).send(p);
			return;
		}
		
		p.challengeInfo.oppId = p.id;
		p.id = opp;
		
		Host.getHost().um.get(p.id).send(p);
	}
	
	public void update(Packet p) {
		// This method sends update packets to any user either playing or watching the game
		// It also updates the game state held on the host
		
		String gId = parts.get(p.id);
		Game g = games.get(gId);
		
		if(g == null) {
			return;
		}
		
		g.update(p);
		
		for(String uid: updators.get(gId)) {
			if(Host.getHost().um.isOnline(uid)) {
				Host.getHost().um.get(uid).send(p);
				
			} else {
				// If the user is not online, remove them from the updators list
				updators.get(gId).remove(uid);
			}
		}
		
		if(p.updateInfo != null) {
			if(p.updateInfo.header == UHeader.win) {
				endGame(g.id);
				
			}
		}
	}
	
	public UserInfo stopGame(UserInfo u1) {
		// This method is called by the command line when an admin stops a game containing user u1
		
		String gId = parts.get(u1.id);
		Game g = games.get(gId);
		
		UserInfo u2 = g.u1.id.equals(u1.id) ? g.u2 : g.u1;
		
		Packet p = new Packet(Header.stopGame);
		for(String id: updators.get(gId)) {
			Host.getHost().um.get(id).send(p);
			
		}

		endGame(gId);
		return u2;
	}
	
	public void endGame(String gId) {
		// This method ends a game, updates statistics and removes the game
		
		Game g = games.get(gId);
		if(g.winnerId != null) {
			updateStats(g, g.winnerId, g.getOppId(g.winnerId));
			
		}
		
		UserDBManager.setIngame(g.u1.id, false);
		UserDBManager.setIngame(g.u2.id, false);
		
		removeGame(gId);
	}
	
	public void removeGame(String gId) {
		// This method removes a game from all relevant locations
		
		Game g = games.get(gId);
		
		parts.remove(g.u1.id);
		parts.remove(g.u2.id);
		games.remove(gId);
		
		for(String id: updators.get(gId)) {
			updateLink.remove(id);
		}
		updators.remove(gId);
		
	}
	
	public void removeUser(String id) {
		// This method removes a user from an updator list and a game (if they are a player in a game)
		
		if(updateLink.containsKey(id)) {
			updateLink.get(id).remove(id);
		}
		
		if(inGame(id)) {
			String gId = parts.get(id);
			Game g = games.get(gId);
			
			Packet p = new Packet(Header.updateGame);
			p.updateInfo = new UpdateInfo(UHeader.win, Win.forfeit, g.getOppId(id));
			Host.getHost().um.get(g.getOppId(id)).send(p);
			
			g.winnerId = g.getOppId(id);
			endGame(gId);
			
		} else {
			pool.remove(id);
		}
	}
	
	private void updateStats(Game g, String wId, String lId) {
		// This method updates the player's statistics after a game
		
		// Current statistics
		UserStats winS = StatsDBManager.getUS(wId); 
		UserStats loseS = StatsDBManager.getUS(lId);
		
		int diffelo = loseS.elo - winS.elo;  // if lose.elo > win.elo => delo > 0
		int deltaElo = Maths.calculateDeltaElo(diffelo); // change in elo
		
		winS.updateElo(deltaElo);
		if(loseS.elo > winS.highestEloVictory) { winS.highestEloVictory = loseS.elo; }
		winS.noGames++;
		winS.noGamesWon++;
		winS.noBallsPotted += winS.id.equals(g.state.redID) ? g.state.red : g.state.yellow;
		winS.noBallsPotted += g.state.win == Win.pottedAll ? 1 : 0; // potted the black as well
		
		loseS.updateElo(-deltaElo);
		loseS.noGames++;
		loseS.noGamesLost++;
		loseS.noBallsPotted += loseS.id.equals(g.state.redID) ? g.state.red : g.state.yellow;
		
		// Update Database with new statistics
		StatsDBManager.setUS(wId, winS);
		StatsDBManager.setUS(lId, loseS);
		
		Host.getHost().um.get(winS.id).updateElo(winS.elo);
		Host.getHost().um.get(loseS.id).updateElo(loseS.elo);
		
	}
	
	
	public boolean inGame(String uid) {
		return parts.containsKey(uid);
		
	}
	
	public String getOpposition(String uid) {
		// Returns a user's opposition's id
		if(inGame(uid)) {
			String gId = parts.get(uid);
			Game g = games.get(gId);
			return g.u1.id.equals(uid) ? g.u2.id : g.u1.id;
		}
		return null;
	}
	
	public void close() {
		// Closes the Manager, clears memory, alerts users
		pool.stop();
		games.forEach((String gid, Game g) -> {
			Packet p = new Packet(Header.stopGame);
			for(String id: updators.get(gid)) {
				Host.getHost().um.get(id).send(p);
				
			}
		});
		
		games.clear();
		parts.clear();
		updators.clear();
		updateLink.clear();
	}

}
