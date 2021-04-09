package samb.host.game;

import java.util.HashMap;

import samb.host.database.UserDBManager;

public class UserManager {
	
	private HashMap<String, User> users;
	
	public UserManager() {
		this.users = new HashMap<>();
		
	}
	
	
	public void add(User u) {
		this.users.put(u.id, u);
		UserDBManager.setOnline(u.id, true);
		
	}
	
	public void remove(String id) {
		this.users.remove(id);
		UserDBManager.setOnline(id, false);
		
	}
	
	public User get(String id) {
		return this.users.get(id);
		
	}
	
	public boolean isOnline(String id) {
		return users.containsKey(id);
		
	}
	
	public void close() {
		this.users.clear();
		
	}

}
