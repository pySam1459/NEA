package samb.host.main;

import java.net.DatagramPacket;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import samb.com.database.UserInfo;
import samb.com.database.UserStats;
import samb.com.server.BaseProcessor;
import samb.com.server.packet.Error;
import samb.com.server.packet.Packet;
import samb.com.server.packet.PacketFactory;
import samb.host.database.StatsDBManager;
import samb.host.database.UserDBManager;
import samb.host.game.GameManager;
import samb.host.game.User;
import samb.host.game.UserManager;

public class Host extends BaseProcessor implements Runnable {
	/* This is the main class for the Host program, which links all the parts to the Host together
	 * This class is a subclass of the abstract BaseProcessor class, so the 'handle' method is defined here,
	 * therefore wherever the Packet data is needed, the Host class can send it there
	 * 
	 * A Command Line Thread will be run to be used by the Admin of the host server
	 * */
	
	private volatile boolean commanding = false;
	private Thread commandThread;
	public Scanner input;
	
	public boolean online = false;
	
	public Server server;
	public UserManager um;
	public GameManager gm;
	
	public Host() {
		UserDBManager.start();
		StatsDBManager.start();
		
		this.um = new UserManager();
		this.gm = new GameManager(this);
		
		start();
		
	}
	
	@Override
	public void run() {
		/* The Command Line is organised by groups, a command will be in 1 of 3 groups: server, database (db), game
		 * Each group has a list of commands which will perform certain functions which will then take certain arguments
		 * An example command could be:  'server start 2001'
		 * This command specifies the 'server' group, the 'start' command, with arguments ['2001']
		 * The command would then be handled and in turn the server would start listening on port 2001
		 * */
		
		this.input = new Scanner(System.in);
		String adminUser = adminLogin();
		
		String command;
		String[] args;
		while(commanding) {
			System.out.printf("%s@Server0:~# ", adminUser);
			command = input.nextLine();
			args = command.split(" ");
			
			if(args.length >= 1) {
				switch(args[0]) {
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
		// This method handles all commands regarding server management
		if(args.length < 2) {
			System.out.println("Invalid Arguments");
			return;
		}
		switch(args[1]) {
		case "start":
			startServer(args);
			online = true;
			break;
			
		case "stop":
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
			// No PORT specified
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
		if(args.length < 3) {
			System.out.println("Invalid Arguments");
			return;
		} else if(online) {
			// When the server is online, errors would be caused if you were to create/delete/reset the database
			switch(args[1]) {
			case "create":
			case "delete":
			case "reset":
				System.out.printf("Cannot perform command '%s' as the Host is currently Online!\n", args[1]);
				return;
			}
		}
		
		// These switch/cases test which command was given by the admin
		switch(args[1]) {
		case "create":
			dbCreateCommand(args);
			break;
			
		case "delete":
			dbDeleteCommand(args);
			break;
			
		case "reset":
			dbResetCommand(args);
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
			
		default:
			System.out.printf("Invalid Command '%s'\n", args[1]);
		}
	}
	
	private void dbCreateCommand(String[] args) {
		// Creates the table specified in 3rd argument
		
		if("users".equals(args[2])) {
			if(UserDBManager.createTable()) {
				System.out.println("Users table has been created");
				
			} else { System.out.println("Users table has NOT been successfully created!"); }
			
		} else if("stats".equals(args[2])) {
			if(StatsDBManager.createTable()) {
				System.out.println("Stats table has been created");
				
			} else { System.out.println("Stats table has NOT been successfully created!"); }
			
		} else { 
			System.out.printf("Invalid Table Name '%s'\n", args[2]); 
		}
	}
	
	private void dbDeleteCommand(String[] args) {
		// Drops the table specified in 3rd argument
		
		if("users".equals(args[2])) {
			if(UserDBManager.dropTable()) {
				System.out.println("Users table has been deleted");
				
			} else { System.out.println("Users table has NOT been successfully deleted!"); }
			
		} else if("stats".equals(args[2])) {
			if(StatsDBManager.dropTable()) {
				System.out.println("Stats table has been deleted");
				
			} else { System.out.println("Stats table has NOT been successfully deleted!"); }
			
		} else { 
			System.out.printf("Invalid Table Name '%s'\n", args[2]); 
		}
	}
	
	private void dbResetCommand(String[] args) {
		// Resets the table specified in 3rd argument
		
		if("users".equals(args[2])) {
			if(UserDBManager.resetTable()) {
				System.out.println("Users table has been reset");
				
			} else { System.out.println("Users table has NOT been successfully reset!"); }
			
		} else if("stats".equals(args[2])) {
			if(StatsDBManager.resetTable()) {
				System.out.println("Stats table has been reset");
				
			} else { System.out.println("Stats table has NOT been successfully reset!"); }
			
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
				orderby += " DESC"; // To order these columns by descending order, means the highest values will be at the top
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
					ui.username, ui.id, online, ui.email, us.elo, us.noGames, us.noGamesWon, us.noGamesLost, us.noBallsPotted, us.highestElo, us.highestEloVictory);
			
		} else {
			System.out.printf("Unknown User '%s'\n", args[2]);
		}
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
			
		default:
			System.out.printf("Invalid Command '%s'\n", args[1]);
		}
	}
	
	private void startGame(String[] args) {
		if(args.length >= 4) {
			UserInfo u1 = UserDBManager.getUIFromName(args[2]);
			UserInfo u2 = UserDBManager.getUIFromName(args[3]);
			
			if(u1 == null) {
				System.out.printf("User '%s' doesn't exists!\n", args[2]);
			} else if(u2 == null) {
				System.out.printf("User '%s' doesn't exists!\n", args[3]);
			} else if(!um.isOnline(u1.id)) {
				System.out.printf("User '%s' is not Online!\n", u1.username);
			} else if(!um.isOnline(u2.id)) {
				System.out.printf("User '%s' is not Online!\n", u2.username);
			} else {
				gm.newGame(u1.id, u2.id);
				System.out.printf("A game has started between %s and %s!\n", u1.username, u2.username);
			}
		}
	}
	
	private void stopGame(String[] args) {
		if(args.length >= 3) {
			UserInfo u = UserDBManager.getUIFromName(args[2]);
			
			if(u == null) {
				System.out.printf("User '%s' doesn't exists!\n", args[2]);
			} else if(!um.isOnline(u.id)) {
				System.out.printf("User '%s' is not Online!\n", args[2]);
			} else {
				UserInfo u2 = gm.stopGame(u);
				System.out.printf("The game between %s and %s has stopped!\n", u.username, u2.username);
			}
		}
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
			
		default:
			System.out.printf("Invalid Command '%s'\n", args[0]);
		}
	}
	

	// Packet handling
	@Override
	public void handle(DatagramPacket packet) {		
		// Received Packets are handled here, their Header is used to identify where the data is needed in the program
		
		Packet p = PacketFactory.toPacket(packet.getData());
		UserInfo ui;
		switch(p.header) {
		case login:
			// This case either authorizes the login of a user, or sends back an invalid details error
			ui = UserDBManager.getUIFromName(p.loginInfo.username);
			if(ui != null) {
				if(ui.password.equals(p.loginInfo.password)) {
					p.id = ui.id;
					loginUser(p, packet);
					return;
				}
			}
			
			p.loginInfo.authorized = false;
			p.loginInfo.err = Error.invalidDetails;
			server.sendTo(p, packet.getAddress(), packet.getPort());
			break;
			
		case signup:
			// This case either signs up the user (storing their details and generating an id), or sends back an error
			ui = UserDBManager.getUIFromName(p.loginInfo.username);
			if(ui == null) {
				String query = String.format("SELECT * FROM users WHERE email='%s';", p.loginInfo.email);
				List<UserInfo> us = UserDBManager.executeQuery(query);
				if(us.size() == 0) {
					signupUser(p, packet);
					return;
					
				} else { p.loginInfo.err = Error.emailTaken; }
			} else { p.loginInfo.err = Error.usernameTaken; }
			
			p.loginInfo.authorized = false;
			server.sendTo(p, packet.getAddress(), packet.getPort());
			break;
			
		case leave:
			// This case updates the host on if a user has closed their client program
			gm.removeUser(p.id);
			um.remove(p.id);
			break;
		
			
		case updateGame:
			// This case receives an update from a user about a new game state, updates the internal game state and sends update packets to the updators
			gm.update(p);
			break;
			
			
		case getStats:
			// This case sends back the stats of a specific user (TODO check if actual user for security)
			p.userStats = StatsDBManager.getUS(p.id);
			server.sendTo(p, packet.getAddress(), packet.getPort());
			break;
			
		case getUpdateGame:
			if(um.isOnline(p.spec)) {
				gm.addSpectate(p.spec, p.gameInfo);
				
			}
			break;
		
		default:
			System.out.printf("Unknown Header '%s'\n", p.header.toString());
		}
	}
	
	private void loginUser(Packet p, DatagramPacket packet) {
		// This method is called by the "case login", which creates a user object and sends an authorizing packet to the user
		User u = new User(this, p, packet);
		um.add(u);
		
		p.loginInfo.authorized = true;
		p.loginInfo.password = null;
		u.send(p);
		
	}
	
	private void signupUser(Packet p, DatagramPacket packet) {
		// This method is called by the "case signup", which generates an ID, adds the user's dtails the the database and sends an authorizing packet to the user
		p.id = UUID.randomUUID().toString();
		
		UserInfo ui = new UserInfo(p.id, p.loginInfo.username, p.loginInfo.email, p.loginInfo.password);
		UserDBManager.addUser(ui);
		StatsDBManager.addUser(new UserStats(p.id));
		
		User u = new User(this, p, packet);
		um.add(u);
		
		p.loginInfo.authorized = true;
		p.loginInfo.password = null;
		u.send(p);
	}
	
	
	// Host start/stop
	public synchronized void start() {
		// Start Command Line
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
			server.stop();
		}
		
		UserDBManager.close();
		StatsDBManager.close();
	}

	public static void main(String[] args) {
		// This method is called first when the program is executed
		new Host();

	}

}
