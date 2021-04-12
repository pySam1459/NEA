package samb.host.main;

import java.net.DatagramPacket;
import java.util.Scanner;
import java.util.UUID;

import samb.com.database.UserInfo;
import samb.com.database.UserStats;
import samb.com.server.BaseProcessor;
import samb.com.server.packet.Error;
import samb.com.server.packet.FHeader;
import samb.com.server.packet.Packet;
import samb.com.server.packet.PacketFactory;
import samb.com.utils.Config;
import samb.com.utils.Func;
import samb.host.database.FriendsDBManager;
import samb.host.database.LoginCredentials;
import samb.host.database.StatsDBManager;
import samb.host.database.UserDBManager;
import samb.host.game.GameManager;
import samb.host.game.User;
import samb.host.game.UserManager;

public class Host extends BaseProcessor implements Runnable {
	/* This is the main class for the Host program, which links all the parts of the Host together
	 * This class is a subclass of the abstract BaseProcessor class, so the 'handle' method is defined here,
	 *   therefore wherever the Packet data is needed, the Host class can send it there
	 * 
	 * A Command Line Thread will be run to be used by the Admin of the host server
	 * */
	
	private volatile boolean commanding = false;
	private Thread commandThread;
	public Scanner input;
	
	public boolean online = false, shutdown = false;
	
	private static Host thisHost;
	public Server server;
	public UserManager um;
	public GameManager gm;
	
	public Host() {
		Host.thisHost = this;
		adminLogin(); // blocks until admin logins in
		
		LoginCredentials.loadCredentials();
		UserDBManager.start();
		StatsDBManager.start();
		FriendsDBManager.start();
		Config.loadConfig();
		
		addShutdownHook();
		start();

	}
	
	@Override
	public void run() {
		/* The Command Line is organised by groups, a command will be in 1 of 3 groups: server, database (db) or game
		 * Each group has a list of commands which will perform certain functions, which takes certain arguments
		 * An example command could be:  'server start 2001'
		 * This command specifies the 'server' group, the 'start' command, with arguments ['2001']
		 * The command would then be handled and in turn the server would start listening on port 2001
		 * */
		
		String command;
		String[] args;
		while(commanding) {
			System.out.printf("console@Server0:~# "); // console prompt
			command = input.nextLine();
			args = command.split(" ");
			
			if(args.length >= 1) {
				switch(args[0]) { // switch statement sends the command to their respective method
				case "server":
					handleServerCommand(args);
					break;
					
				case "database":
				case "db":
					handleDatabaseCommand(args);
					break;
					
				case "game":
					handleGameCommand(args);
					break;
				
				default:
					handleMiscCommand(args);
				}
			} else {
				System.out.println("Invalid Command");
			}
		}
	}
	
	private String adminLogin() {
		// For demonstation purposes, admin credentials are {"admin", "password"}

		this.input = new Scanner(System.in);
		String username, password;
		while(true) {
			System.out.print("Username >> ");
			username = input.nextLine();
			if(username.length() == 0) { System.exit(0); }
			
			System.out.print("Password >> ");
			password = input.nextLine();
			
			if("admin".equals(username) && "password".equals(password)) {
				return username;
			} else {
				System.out.println("Invalid Credentials");
			}
		}
	}
	
	
	// Command Line Group: Server
	private void handleServerCommand(String[] args) {
		// This method handles all commands regarding the server
		
		if(args.length < 2) {
			System.out.println("Invalid Arguments");
			return;
		}
		// The switch statement tests which command was given by the admin
		switch(args[1]) {
		case "start":
			this.um = new UserManager();
			this.gm = new GameManager();
			startServer(args);
			online = true;
			break;
			
		case "stop":
			gm.close();
			um.close();
			server.stop();
			this.server = null;
			
			System.out.println("Server has Stopped!");
			online = false;
			break;
			
		default:
			System.out.printf("Invalid Command '%s'\n", args[1]);
		}
	}
	
	private void startServer(String[] args) {
		if(args.length == 2) {
			// No PORT specified, therefore default port 5303
			this.server = new Server(this);
			server.start(5303);
			
		} else if(args[2].matches("\\d+")) {
			int port = Integer.parseInt(args[2]);
			if(0 <= port && port <= 65535) {
				// PORT specified
				this.server = new Server(this);
				server.start(port);
				
			} else { 
				System.out.println("Ports must be between 0-65535"); 
			}
		} else { 
			System.out.println("Invalid Port");
		}
	}
	
	
	// Command Line Group: Database
	private void handleDatabaseCommand(String[] args) {
		// This method handles all database commands
		
		if(args.length < 3 && !"setup".equals(args[1])) {
			System.out.println("Invalid Arguments");
			return;
		} else if(online) {
			// When the server is online, errors would be caused if you were to create/delete/setup the database
			switch(args[1]) {
			case "create":
			case "delete":
			case "setup":
				System.out.printf("Cannot perform command '%s' as the Host is currently Online!\n", args[1]);
				return;
			}
		}
		
		switch(args[1]) {
		case "create":
			dbCreateCommand(args);
			break;
			
		case "delete":
			dbDeleteCommand(args);
			break;
			
		case "remove":
			dbRemoveCommand(args);
			break;
			
		case "display":
			dbDisplayCommand(args);
			break;
		
		case "details":
			dbDetailsCommand(args);
			break;
			
		case "setup":
			dbSetup();
			break;
			
		default:
			System.out.printf("Invalid Command '%s'\n", args[1]);
		}
	}
	
	private void dbCreateCommand(String[] args) {
		// Creates the table specified in 3rd argument
		// The static methods return a boolean, if database update was successful
		
		if("users".equals(args[2])) {
			if(UserDBManager.createTable()) {
				System.out.println("Users table has been created");
				
			} else { System.out.println("Users table has NOT been successfully created!"); }
			
		} else if("stats".equals(args[2])) {
			if(StatsDBManager.createTable()) {
				System.out.println("Stats table has been created");
				
			} else { System.out.println("Stats table has NOT been successfully created!"); }
			
		} else if("friends".equals(args[2])) {
			if(FriendsDBManager.createAll()) {
				System.out.println("Friend tables have been created");
				
			} else { System.out.println("Friend tables have NOT been successfully created!"); }
			
		} else { 
			System.out.printf("Invalid Table Name '%s'\n", args[2]); 
		}
	}
	
	private void dbDeleteCommand(String[] args) {
		// Drops the table specified in 3rd argument
		
		if("users".equals(args[2])) {
			if(UserDBManager.dropTable() && FriendsDBManager.dropAll()) {
				System.out.println("Users table has been deleted");
				
			} else { System.out.println("Users table has NOT been successfully deleted!"); }
			
		} else if("stats".equals(args[2])) {
			if(StatsDBManager.dropTable()) {
				System.out.println("Stats table has been deleted");
				
			} else { System.out.println("Stats table has NOT been successfully deleted!"); }
			
		} else if("friends".equals(args[2])) {
			if(FriendsDBManager.dropAll()) {
				System.out.println("Friends table has been deleted");
				
			} else { System.out.println("Friend tables have NOT been successfully deleted!"); }
			
		} else { 
			System.out.printf("Invalid Table Name '%s'\n", args[2]); 
		}
	}
	
	private void dbRemoveCommand(String[] args) {
		// Removes a specified user from all tables
		
		String username = args[2];
		UserInfo ui = UserDBManager.getUIFromName(username);
		if(ui != null) {
			UserDBManager.removeUser(ui.id);
			StatsDBManager.removeUser(ui.id);
			FriendsDBManager.removeUser(ui.id);
			System.out.printf("User '%s' removed!\n", username);
			
		} else {
			System.out.printf("User '%s' does not exist!\n", username);
		}
	}
	
	private void dbDisplayCommand(String[] args) {
		// Displays all the data in the table specified in 3rd argument
		
		String orderby = "username";
		if(args.length == 4) {
			orderby = args[3];
			if(orderby.matches("(elo|noGames|noGamesWon|noGamesLost|noBallsPotted|highestElo|highestEloVictory)")) {
				// To order these columns by descending order, means the highest values will be at the top
				orderby += " DESC"; 
			}
		}
		
		if("users".equals(args[2])) {
			UserDBManager.display(orderby);
			
		} else if("stats".equals(args[2])) {
			StatsDBManager.display(orderby);
			
		} else { 
			System.out.printf("Invalid Table Name '%s'\n", args[2]); 
		}
	}
	
	private void dbDetailsCommand(String[] args) {
		// Displays the details of a User specified in 3rd argument
		
		UserInfo ui = UserDBManager.getUIFromName(args[2]);
		if(ui != null) {
			UserStats us = StatsDBManager.getUS(ui.id);
			boolean online = um.isOnline(ui.id);
			System.out.printf("%s {\n  ID: %s\n  Online: %s\n  Email: %s\n  Elo: %d\n  # Games: %d\n  # Games Won: %d"
					+ "\n  # Games Lost: %d\n  # Balls Potted: %d\n  Highest Elo: %d\n  HighestEloVictory: %d\n}\n", 
					ui.username, ui.id, online, ui.email, us.elo, us.noGames, us.noGamesWon, us.noGamesLost, 
					us.noBallsPotted, us.highestElo, us.highestEloVictory);
			
		} else {
			System.out.printf("Unknown User '%s'\n", args[2]);
		}
	}
	
	private void dbSetup() {
		// Drops all tables, and then creates them
		
		FriendsDBManager.dropAll();
		StatsDBManager.dropTable();
		UserDBManager.dropTable();
		
		UserDBManager.createTable();
		StatsDBManager.createTable();
		FriendsDBManager.createAll();
		
		System.out.println("The Database is Setup!");
	}
	
	
	// Command Line Group: Game
	private void handleGameCommand(String[] args) {
		// This method handles all commands regarding game management
		if(args.length < 2) {
			System.out.println("Invalid Arguments");
			return;
		}
		
		switch(args[1]) {
		case "start":
			startGame(args);
			break;
		
		case "stop":
			stopGame(args);
			break;
			
		case "spectate":
			specGame(args);
			break;
			
		default:
			System.out.printf("Invalid Command '%s'\n", args[1]);
		}
	}
	
	private void startGame(String[] args) {
		// This method starts a game between args[2] and args[3]
		
		if(args.length >= 4) {
			UserInfo u1 = validUser(args[2]);
			UserInfo u2 = validUser(args[3]);
			
			if(u1 != null && u2 != null) {
				gm.newGame(u1.id, u2.id);
				System.out.printf("A game has started between %s and %s!\n", u1.username, u2.username);
				
			}
		} else {
			System.out.println("Invalid Arguements");
		}
	}
	
	private void stopGame(String[] args) {
		// This method stops the game args[2] is playing in
		
		if(args.length >= 3) {
			UserInfo u = validUser(args[2]);
			if(u != null){
				UserInfo u2 = gm.stopGame(u);
				System.out.printf("The game between %s and %s has stopped!\n", u.username, u2.username);
				
			}
		} else {
			System.out.println("Invalid Arguements");
		}
	}
	
	private void specGame(String[] args) {
		// This method 
		if(args.length >= 4) {
			UserInfo u1 = validUser(args[2]);
			UserInfo u2 = validUser(args[3]);
			
			if(u1 != null && u2 != null) {
				gm.queueSpectate(u2.id, u1.id);
				System.out.printf("%s is now spectating %s!\n", u1.username, u2.username);
				
			}
		} else {
			System.out.println("Invalid Arguments");
		}
	}
	
	private UserInfo validUser(String username) {
		// This method returns a UserInfo if the user is 'valid', otherwise outputs an error message
		// A user is valid if they exist, are online and aren't in a game
		UserInfo u = UserDBManager.getUIFromName(username);
		
		if(u == null) {
			System.out.printf("User %s does not exist!\n", username);
		} else if(!um.isOnline(u.id)) {
			System.out.printf("User %s is not online!\n", username);
		} else if(gm.inGame(u.id)) {
			System.out.printf("User %s is already in a game!\n", username);
		} else {			
			return u;
		}
		return null;
	}
	
	
	// Command Line Group: Misc
	private void handleMiscCommand(String[] args) {
		// This method handles any other command which isn't in any group
		
		switch(args[0]) {
		case "exit":
		case "quit":
			stop();
			System.out.println("Exiting Console");
			System.exit(0);
			break;
			
		case "pool":
			gm.pool.showBands();
			break;
			
		default:
			System.out.printf("Invalid Command '%s'\n", args[0]);
		}
	}
	

	// Packet handling
	@Override
	public void handle(DatagramPacket packet) {
		// This method is called by the Server's listening thread, when it receives a new Packet
		// The received packets are handled here, their Header is used to identify where the data is needed in the program
		
		Packet p = PacketFactory.toPacket(packet.getData());
		switch(p.header) {
		// Login cases
		case login:
			// This case either authorizes the login of a user, or sends back an invalid details error
			loginCase(p, packet);
			break;
			
		case signup:
			// This case either signs up the user (storing their details and generating an id), or sends back an error
			signupCase(p, packet);
			break;
			
		case leave:
			// This case updates the host on if a user has closed their client program
			gm.removeUser(p.id);
			um.remove(p.id);
			break;
			
			
		// Game Cases
		case joinPool:
			// This case adds the user to the game pool, for matchmaking
			gm.pool.add(p.id);
			break;
		
		case leavePool:
			// This case removes the user from the game pool
			gm.pool.remove(p.id);
			break;
			
		case spectate:
			// This case is called by a user wanting to spectate another user
			gm.queueSpectate(p.spec, p.id);
			break;
			
		case challenge:
			// This case either issues a challenge or creates a new game (if challenge was accepted)
			if(p.challengeInfo.accepted) {
				if(um.isOnline(p.challengeInfo.oppId) && um.isOnline(p.id)) {
					gm.newGame(p.challengeInfo.oppId, p.id);
					
				}
			} else {
				gm.challenge(p);
			}
			break;
		
			
		// Update Cases
		case updateGame:
			// This case receives an update from a user about a new game state, 
			//   updates the internal game state and sends update packets to the updators
			gm.update(p);
			break;
			
		case getUpdateGame:
			// This case adds a spectator to a game with the gameInfo received from a player of that game
			if(um.isOnline(p.spec)) {
				gm.addSpectate(p);
				
			}
			break;
			
			
		// Getter Cases
		case getStats:
			// This case sends back the stats of a specified user
			getStats(p);
			break;
			
		case getFriends:
			// This case sends back a user's friends list, or a list of users searched by the user
			getFriends(p);
			break;
			
		
		// Misc Cases
		case chat:
			// This case sends the chat of a player to their opposition
			String oid = gm.getOpposition(p.id);
			if(oid != null) {
				um.get(oid).send(p);
			}
			break;
			
		case addFriend:
			// This case informs the host of a new friendship
			FriendsDBManager.addFriend(p.id, p.friendsInfo.f.id);
			FriendsDBManager.addFriend(p.friendsInfo.f.id, p.id);
			break;
		
			
		default:
			System.out.printf("Unknown Header '%s'\n", p.header.toString());
		}
	}

	
	private void loginCase(Packet p, DatagramPacket packet) {
		// This method checks the authenticity of a users credentials, and either logins in or rejects the user
		
		UserInfo ui = UserDBManager.getUIFromName(p.loginInfo.username);
		if(ui != null) { // valid user
			if(!ui.online) { // is not already online
				if(ui.password.equals(p.loginInfo.password)) { // correct password hash
					p.id = ui.id;
					loginUser(p, packet);
					return;
					
				} else { p.loginInfo.err = Error.invalidDetails; }
			} else { p.loginInfo.err = Error.alreadyOnline; }
		} else { p.loginInfo.err = Error.invalidDetails; }
		
		p.loginInfo.authorized = false;
		server.sendTo(p, packet.getAddress(), packet.getPort());
	}
	
	private void loginUser(Packet p, DatagramPacket packet) {
		// This method is called by the "case login", 
		//   which creates a user object and authorizes the user to join
		
		User u = new User(this, p, packet);
		um.add(u);
		
		p.loginInfo.authorized = true;
		p.loginInfo.password = null;
		u.send(p);
		
	}
	
	private void signupCase(Packet p, DatagramPacket packet) {
		// This method checks the uniqueness and validity of the users credentials,
		//   signing them up, or rejecting them
		
		if(!UserDBManager.exists("username", p.loginInfo.username)) {
			if(!UserDBManager.exists("email", p.loginInfo.email)) {
				if(!Func.isFlag(p.loginInfo.username)) {
					signupUser(p, packet);
					return;
					
				} else {p.loginInfo.err = Error.usernameTaken; }
			} else { p.loginInfo.err = Error.emailTaken; }
		} else { p.loginInfo.err = Error.usernameTaken; }
		
		p.loginInfo.authorized = false;
		server.sendTo(p, packet.getAddress(), packet.getPort());
	}
	
	private void signupUser(Packet p, DatagramPacket packet) {
		// This method is called by the "case signup", which generates an ID, 
		//   adds the user's details the the database and authorizes the user to sign up
		p.id = UUID.randomUUID().toString();
		
		// add user to database
		UserInfo ui = new UserInfo(p.id, p.loginInfo.username, p.loginInfo.email, p.loginInfo.password);
		UserDBManager.addUser(ui);
		StatsDBManager.addUser(new UserStats(p.id));
		FriendsDBManager.addUser(p.id);
		
		User u = new User(this, p, packet);
		um.add(u);
		
		// logins in user
		p.loginInfo.authorized = true;
		p.loginInfo.password = null;
		u.send(p);
	}
	
	private void getStats(Packet p) {
		// Sends a packet containing a user's statistics
		if(um.isOnline(p.id)) {
			if(p.friendsInfo != null) {
				p.userStats = StatsDBManager.getUS(p.friendsInfo.f.id);
				p.friendsInfo.f = FriendsDBManager.get(p.id, p.friendsInfo.f.id); // get friend info as well
				
			} else {
				p.userStats = StatsDBManager.getUS(p.id);
			}
			um.get(p.id).send(p);
			
		}
	}
	
	private void getFriends(Packet p) {
		// Sends a packet containing friends information
		
		if(um.isOnline(p.id)) {
			if(p.friendsInfo.header == FHeader.getFriends) { // friends list
				p.friendsInfo.friends = FriendsDBManager.getAll(p.id);
				um.get(p.id).send(p);
				
			} else if(p.friendsInfo.header == FHeader.searchFriend) { // search results list
				p.friendsInfo.friends = FriendsDBManager.findFriends(p.id, p.friendsInfo.search);
				um.get(p.id).send(p);
				
			}
		}
	}
	
	
	// Host start/stop
	public synchronized void start() {
		// Start Command Line
		shutdown = false;
		commanding = true;
		commandThread = new Thread(this, "Command Line Thread");
		commandThread.start();
	}
	
	public synchronized void stop() {
		// Stop Command Line and other processes
		if(commanding) {
			commanding = false;
			commandThread.interrupt();
			
		} if(server != null) {
			gm.close();
			um.close();
			server.stop();
		}
		
		UserDBManager.close();
		StatsDBManager.close();
		FriendsDBManager.close();
		shutdown = true;
	}
	
	public void addShutdownHook() {
		// This method adds a shutdown hook, if the program is exited, this function will be called
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		    public void run() {
		        if(!shutdown) {
		        	stop();
		        	
		        }
		    }
		}));
	}
	
	// Little trick to get Host instance from a static object
	public static Host getHost() {
		return thisHost;
	}

	public static void main(String[] args) {
		// This method is called first when the program is executed
		new Host();

	}

}
