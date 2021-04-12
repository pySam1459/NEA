package samb.com.database;

import java.io.Serializable;

import samb.com.server.info.ChallengeInfo;

public class Friend implements Serializable {
	/* This class contains information about a 'Friend'
	 *   A friend instance will be created when a row is selected from a 'Friend_$id' table
	 * */
	
	private static final long serialVersionUID = 8937817757063002987L;
	public String id, username;
	public boolean online = false, inGame=false, isFriend=true, challenged=false;
	public ChallengeInfo ci;
	
	public Friend(String id) {
		this.id = id;
		
	}
	
	public Friend(String id, String username) {
		this.id = id;
		this.username = username;
	}
	
	public Friend(String id, String username, boolean online, boolean inGame, boolean isFriend) {
		this.id = id;
		this.username = username;
		this.online = online;
		this.inGame = inGame;
		this.isFriend = isFriend;
		
	}
	
}
