package samb.com.database;

import java.io.Serializable;

public class Friend implements Serializable {
	/* This class contains information about a 'Friend'
	 *   A friend instance will be created when data is selected from a 'Friend_$id' table
	 * */
	
	private static final long serialVersionUID = 8937817757063002987L;
	public String id, username;
	public boolean online = false, inGame=false;
	
	public Friend(String id) {
		this.id = id;
		
	}
	
	public Friend(String id, String username) {
		this.id = id;
		this.username = username;
	}
	
	public Friend(String id, String username, boolean online) {
		this.id = id;
		this.username = username;
		this.online = online;
		
	}
	
}
