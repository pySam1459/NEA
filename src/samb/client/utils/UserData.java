package samb.client.utils;

import samb.com.database.UserInfo;
import samb.com.database.UserStats;
import samb.com.server.info.FriendsInfo;
import samb.com.server.info.GameInfo;
import samb.com.server.info.GameState;

public class UserData {
	/* This class contains the user's data which is to be used in menus, settings, rendering, etc
	 * */
	
	public String id;
	public UserInfo userInfo;
	public UserStats userStats;
	public GameInfo gameInfo;
	public GameState gameState;
	public FriendsInfo friends;

}
