package samb.com.database;

import java.io.Serializable;

public class Friend implements Serializable {
	
	private static final long serialVersionUID = 8937817757063002987L;
	public String id, username;
	public boolean online = false;
	
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
