package samb.host.game;

import java.util.HashMap;

public class UserManager {
	
	private HashMap<String, User> users;
	
	public UserManager() {
		this.users = new HashMap<>();
		
	}
	
	
	public void add(User u) {
		this.users.put(u.id, u);
		
	}
	
	public void remove(String id) {
		this.users.remove(id);
		
	}
	
	public User get(String id) {
		return this.users.get(id);
		
	}
	
	public boolean isOnline(String id) {
		return users.containsKey(id);
		
	}

}
