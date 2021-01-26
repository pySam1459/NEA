package samb.host.game;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import samb.com.database.UserInfo;
import samb.com.server.info.GameInfo;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;
import samb.com.utils.enums.TableUseCase;
import samb.host.main.Host;

public class GameManager {
	/* This class handles game events, new games, updating games, spectating games, ending games, etc
	 * This class also handles who is playing / watching games
	 * The system to record this revolves around a Game ID ('gId') which links game objects and 'updators' to a game
	 * The updators map contains data revolving around who needs to be updated when a player does something in their game
	 * */
	
	private HashMap<String, Game> games;
	private HashMap<String, String> parts;  // Participants, {key: userId, Value: gameId}
	private HashMap<String, CopyOnWriteArrayList<String>> updators;
	// The class 'CopyOnWriteArrayList' is a list which deals with any concurrency issues which may arise
	// For example, if you remove an element from the list during an iteration of that list
	// In a regular list, this would produce an error, but a COWAL deals with this internally
	
	private Host host;
	
	public GameManager(Host h) {
		this.games = new HashMap<>();
		this.parts = new HashMap<>();
		this.updators = new HashMap<>();
		
		this.host = h;
	}
	
	public void newGame(String u1, String u2) {
		// This method creates a new game between user u1 and user u2
		
		String id = UUID.randomUUID().toString();
		Game g = new Game(id, u1, u2);
		games.put(id, g);
		
		parts.put(u1, id);
		parts.put(u2, id);
		
		updators.put(id, new CopyOnWriteArrayList<String>());
		updators.get(id).add(u1);
		updators.get(id).add(u2);
		
		Packet p = new Packet(Header.newGame);
		p.gameInfo = g;
		p.gameInfo.tuc = TableUseCase.playing; // Only for the players
		
		host.um.get(u1).send(p);
		host.um.get(u2).send(p);
		
	}
	
	public void queueSpectate(String uToSpec, String spec) {
		// Since the game state must be requested from a player, the spectator must wait for them to reply
		host.um.get(spec).waiting = true;
		
		Packet p = new Packet(Header.getUpdateGame);
		p.spec = spec;
		
		host.um.get(uToSpec).send(p);
		
	}
	
	public void addSpectate(String spec, GameInfo gi) {
		// Once a player has replied, the spectate can watch the match
		// Check if gi != null
		updators.get(gi.id).add(spec);
		
		Packet p = new Packet(Header.spectate);
		p.gameInfo = gi;
		p.gameInfo.tuc = TableUseCase.spectating; // Only for spectators
		
		host.um.get(spec).send(p);
		
	}
	
	public void update(Packet p) {
		// This method sends update packets to any user either playing or watching the game
		// It also updates the game state held on the host
		
		String gId = parts.get(p.id);
		Game g = games.get(gId);
		
		g.update(p);
		
		for(String uid: updators.get(gId)) {
			if(host.um.isOnline(uid)) {
				host.um.get(uid).send(p);
				
			} else {
				// If the user is not online, remove them from the updators list
				updators.get(gId).remove(uid);
			}
		}
	}
	
	public UserInfo stopGame(UserInfo u1) {
		String gId = parts.get(u1.id);
		Game g = games.get(gId);
		
		UserInfo u2 = g.u1.id.equals(u1.id) ? g.u2 : g.u1;
		
		Packet p = new Packet(Header.stopGame);
		for(String id: updators.get(gId)) {
			host.um.get(id).send(p);
			
		}

		endGame(gId);
		
		return u2;
		
	}
	
	public void endGame(String gId) { // TODO might rename to removeGame, endGame maybe for end states
		Game g = games.get(gId);
		
		parts.remove(g.u1.id);
		parts.remove(g.u2.id);
		games.remove(gId);
		
		updators.remove(gId);
		
		// TODO send updates and update stats db
		// if game.win == user, send update
		
	}
	
	public void removeUser(String id) {
		if(inGame(id)) {
			String gId = parts.get(id);
			games.get(gId).abandoner = id;
			endGame(gId);
			
		}
	}
	
	public boolean inGame(String uid) {
		return parts.containsKey(uid);
		
	}

}
