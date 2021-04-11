package samb.com.server.info;

import java.io.Serializable;
import java.util.List;

import samb.com.database.Friend;
import samb.com.server.packet.FHeader;

public class FriendsInfo implements Serializable {
	// This class contains information about a user's friends
	// This data is requested and sent back to and by the host
	
	private static final long serialVersionUID = -6950454630529384230L;
	public FHeader header;
	public List<Friend> friends;
	
	public Friend f;
	public String search;
	
	public FriendsInfo(FHeader header, List<Friend> friends) {
		this.header = header;
		this.friends = friends;
		
	}
	
	public FriendsInfo(FHeader header, String str) {
		this.header = header;
		
		if(header == FHeader.getStats || header == FHeader.addFriend) {
			this.f = new Friend(str);
			
		} else if(header == FHeader.searchFriend) {
			this.search = str;
			
		}
	}
	
}
