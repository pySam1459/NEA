package samb.com.server.packet;

public enum Header {
	/* This Header enum contains values which will be used to identify the purpose/function of a Packet object. */

	signup,
	login,
	leave,
	
	joinPool,
	leavePool,
	spectate,
	challenge,
	
	newGame,
	stopGame,
	
	updateGame,
	chat,
	
	getUpdateGame,
	getStats,
	getFriends,
	addFriend;
	
}
