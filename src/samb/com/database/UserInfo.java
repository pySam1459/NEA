package samb.com.database;

import java.io.Serializable;

public class UserInfo implements Serializable {
	// This class contains data about a specific user (determined by the id)
	// A single row in the user table will be 1 UserInfo object
	
	private static final long serialVersionUID = -1335445462934410453L;
	public String id, username, email, password;

	public UserInfo() {}
	
	public UserInfo(String id, String username, String email, String password) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		
	}
	
	public UserInfo(String id, String username) {
		this.id = id;
		this.username = username;
		
	}
}
