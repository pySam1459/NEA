package samb.com.server.packet;

public enum Error {
	// This enum lists any errors which could occur and handled during the runtime of the program

	// logging in/ signing up
	invalidDetails,
	usernameTaken,
	emailTaken,
	alreadyOnline,
	
	// challenge
	alreadyInGame,
	notOnline;
	
}
