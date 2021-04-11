package samb.client.main;

import java.awt.Graphics2D;
import java.net.DatagramPacket;

import samb.client.page.GamePage;
import samb.client.page.LoginPage;
import samb.client.page.MenuPage;
import samb.client.page.PageManager;
import samb.client.page.widget.FriendList;
import samb.client.server.Server;
import samb.client.utils.Consts;
import samb.client.utils.UserData;
import samb.client.utils.inputs.Keyboard;
import samb.client.utils.inputs.Mouse;
import samb.com.database.UserInfo;
import samb.com.server.BaseProcessor;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;
import samb.com.server.packet.PacketFactory;
import samb.com.utils.Config;
import samb.com.utils.Func;

public class Client extends BaseProcessor implements Runnable {
	/* This is the main Client class, when the program is run the main method will be called and the Client will start.
	 * This is where all incoming DatagramPackets from the Host will be handled and directed to their respective "area".
	 * Other classes can call Client.getClient() to use the client's non-static objects{server, window, mouse, 
	 *                                                                     keyboard, userData, pageManager, etc}
	 * */
	
	public static final double TPS = 120.0;
	public static double dt = Consts.DT_CONST / TPS;
	private volatile boolean running = false;
	private Thread mainThread;
	
	public Server server;
	public PageManager pm;
	public UserData udata;
	
	private static Client thisClient;
	private static Window window;
	private static Mouse mouse;
	private static Keyboard keyboard;
	
	
	public Client() {
		Client.thisClient = this;
		Config.loadConfig();
		
		this.server = new Server();
		Client.window = new Window();
		
		Func.loadFonts();
		
		this.pm = new PageManager();
		this.udata = new UserData();
		
		Client.mouse = new Mouse();
		Client.keyboard = new Keyboard();
		
		start();
	}
	
	private void tick() {
		// This method is called $TPS per second
		// Any objects which requires 'updating', ie balls moving, widget animations, etc
		//    will be called in this method

		pm.tick();
		
		Client.mouse.update();
		Window.updateBackground();
		super.process(); // BaseProcessor handles received packets
	}
	
	private void render() {
		// When rendering, the Window class creates a 'Graphics2D' object, which acts as a canvas to draw onto
		// Any object which requires rendering will render onto this Graphics2D object, 
		//   which will then be rendered onto the window frame (ie the screen)
		// Order of rendering is very important as objects which are rendered first will be drawn over by later objects
		
		Graphics2D g = window.getGraphics();  // Get 'canvas' to draw on
		
		pm.render(g); // Renders the page being show, or a transition, or both
		
		window.render();  // Renders 'painted canvas' to screen
		
	}
	
	
	@Override
	public void handle(DatagramPacket packet) {
		// This methods handles incoming DatagramPackets from the Host Server
		// The data of a DatagramPacket is a serialized Packet object
		// Each Packet has a header which is used to identify the purpose of the packet, 
		//     ie login, sign-Up, gameUpadate, etc
		// the switch/case statement will direct each packet (using the header) to wherever it is needed
		
		LoginPage lp;
		MenuPage mp;
		GamePage gp;
		
		Packet p = PacketFactory.toPacket(packet.getData());
		switch(p.header) {
		case login:
			// This case receives from the host whether the user is authorized to continue
			if(p.loginInfo.authorized) {
				udata.id = p.id;
				udata.userInfo = new UserInfo(p.id, p.loginInfo.username);
				pm.changePage(new MenuPage()); // continues to menu page
				
			} else {
				lp = (LoginPage) pm.get();
				lp.setError(p.loginInfo.err); // displays an error
			}
			break;
			
		case signup:
			// This case receives from the host whether the user has been signed up
			if(p.loginInfo.authorized) {
				udata.id = p.id;
				udata.userInfo = new UserInfo(p.id, p.loginInfo.username);
				pm.changePage(new MenuPage()); // continues to menu page
				
			} else {
				lp = (LoginPage) pm.get();
				lp.setError(p.loginInfo.err); // displays an error
			}
			break;
			
			
		case newGame:
			// This case starts a new game 
			if(!pm.isId("GamePage") ) { // checks that the current page is NOT the Game Page
				pm.changePage(new GamePage()); // if it isn't, switch to the Game Page
			}

			gp = (GamePage) pm.get();
			gp.start(p.gameInfo, p.gameState); // starts a new game
			
			break;
			
		case updateGame:
			// This case updates the on going game with the latest move
			if(!pm.isId("GamePage") ) {
				pm.changePage(new GamePage());
			}
			
			gp = (GamePage) pm.get();
			gp.updateTable(p);
			break;
			
		case getUpdateGame:
			// This case sends the current state of the table to the server (used for new spectators)
			//   since the host doesn't store the current table state, a player has to inform a spectator
			if(pm.isId("GamePage") ) {
				gp = (GamePage) pm.get();
				p.gameInfo = gp.getUpdate().gameInfo;
				p.gameState = gp.getUpdate().gameState;
				
			} else {
				p.gameInfo = null;
				p.gameState = null;
			}
			
			server.send(p);
			break;
			
		case stopGame:
			// This case returns from a gamePage to the menuPage
			if(pm.isId("GamePage")) {
				pm.changePage(new MenuPage());
				
			}
			break;
		
		case spectate:
			// This case set the table to the current position of the spectated table
			if(!pm.isId("GamePage")) {
				pm.changePage(new GamePage());
			}

			gp = (GamePage) pm.get();
			gp.spectate(p.gameInfo, p.gameState);
			break;
			
		case challenge:
			// This case receives a challenge from the host
			if(pm.isId("MenuPage")) {
				mp = (MenuPage) pm.get();
				mp.recvChallenge(p.challengeInfo);
			}
			break;
			
			
		case chat:
			// This case receives a chat message and adds it to the players chatbox
			if(pm.isId("GamePage")) {
				gp = (GamePage) pm.get();
				gp.addChat(p.message);
			}
			break;
			
			
		case getStats:
			// This case updates the userData of the user's stats
			//   or the stats of a friend/to-be-friend
			if(p.userStats == null) { break; }
			if(pm.isId("MenuPage")) {
				mp = (MenuPage)pm.get();
				if(p.friendsInfo != null) {
					mp.prof.setStats(p.userStats, true);
					break;
					
				}
			}
			
			udata.userStats = p.userStats;
			break;
			
		case getFriends:
			// This case updates the udata of the user's friends
			if(pm.isId("MenuPage")) {
				mp = (MenuPage)pm.get();
				((FriendList) mp.get("friendList")).setFriends(p);
			}
			break;
		
		default:
			System.out.printf("Unknown header '%s'\n", p.header);
		
		}
	}
	
	
	@Override
	public void run() {
		/* This is the programs main loop. TPS is Ticks Per Second, so the methods 'tick' and 'render' will be called $Client.TPS$ number of times per second
		 * The render method will be called after the tick method has finished 
		 * so there aren't any concurrency issues, also why the tick and render methods are called in the same Thread
		 * */
		double ns = 1000000000.0 / TPS;  // number of nanoseconds between each tick
		double delta = 0.0;
		long now, lastTime = System.nanoTime();
		while(running) {
			now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while(delta >= 1.0) {
				tick();
				render();
				
				delta--;
			} 
		}
	}
	
	public synchronized void start() {
		// This method starts all the threads necessary
		server.start();
		window.start();
		
		running = true;
		mainThread = new Thread(this, "Main Thread");
		mainThread.start();
		
	}
	
	public synchronized void stop() {
		// This method will inform the host server that this client is stopping and will close any threads or processes running
		informHost();
		
		running = false;
		mainThread.interrupt();
		
		window.stop();
		server.stop();
		
	}
	
	private void informHost() {
		if(udata.id != null) {
			Packet p = new Packet(Header.leave);
			server.send(p);
		}
	}
	
	// Getters
	public static Client getClient() {
		// A little trick I learnt from reading the Minecraft source code was to set a static variable to the main class instance
		// Therefore, the main class instance does not need to be passed as a parameter but instead is returned by 'Client.getClient()'
		return Client.thisClient;
	}
	
	public static Window getWindow() {
		return Client.window;
	}
	
	public static Mouse getMouse() {
		return Client.mouse;
	}
	
	public static Keyboard getKeyboard() {
		return Client.keyboard;
	}
	
	
	public static void main(String[] args) {
		// This is the main function, called when the program starts, creating a new Client instance
		new Client();
		
	}

}
