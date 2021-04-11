package samb.com.server.info;

import java.io.Serializable;
import java.util.List;

import samb.com.database.Friend;

public class FriendsInfo implements Serializable {
	// This class contains information about a user's friends
	// This data is requested and sent back to and by the host
	
	private static final long serialVersionUID = -6950454630529384230L;
	public List<Friend> friends;
	public String friendId;
	public boolean online=false, inGame=false;
	
	
	public FriendsInfo(List<Friend> friends) {
		this.friends = friends;
		
	}
	
	public FriendsInfo(String id) {
		this.friendId = id;
		
	}
	
}
