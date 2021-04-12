package samb.com.server.packet;

import java.io.Serializable;

import samb.com.database.UserStats;
import samb.com.server.info.ChallengeInfo;
import samb.com.server.info.FriendsInfo;
import samb.com.server.info.GameInfo;
import samb.com.server.info.GameState;
import samb.com.server.info.LoginInfo;
import samb.com.server.info.Message;
import samb.com.server.info.UpdateInfo;

public class Packet implements Serializable {
	/* The Packet Class is critical to the programs ability to communicate across the Internet.
	 * A Packet Object contains data to be sent across the Internet, to the host server or to a client.
	 * This host server will handle the packet, either by processing it or redirecting it to another client.
	 * This data is used to synchronized game play between players across the Internet and requesting user information.
	 * The 'Serializable' interface allows this class to be converted into a byte array 
	 *     (which can then be sent across the Internet).
	 * */

	private static final long serialVersionUID = -8972345639886828446L;
	
	// This Packet Header is used by the receiving entity to determine the purpose of the packet
	//   (either synchronization data, login/sign-up details, statistical data, etc)
	public Header header; 
	
	// Each player has a unique id, which can be used to identify 
	//   each player in a database / HashMap / or other data holding structures.
	public String id;      
	public String spec;    // Spectator ID
	
	// Each 'Info' class contains information about their prefix
	public LoginInfo loginInfo;
	public GameInfo gameInfo;
	public GameState gameState;
	public UpdateInfo updateInfo;
	public UserStats userStats;
	public ChallengeInfo challengeInfo;
	public FriendsInfo friendsInfo;
	public Message message;
	
	public Packet(Header header) {
		this.header = header;
		
	}

}
