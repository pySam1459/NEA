package samb.com.server.info;

import java.io.Serializable;
import java.util.List;

import samb.com.database.Friend;

public class FriendsInfo implements Serializable {
	
	private static final long serialVersionUID = -6950454630529384230L;
	public List<Friend> friends;
	
}
