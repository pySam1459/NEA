package samb.client.main;

import java.awt.Graphics2D;
import java.net.DatagramPacket;

import samb.client.game.GamePage;
import samb.client.page.LoginPage;
import samb.client.page.MenuPage;
import samb.client.page.PageManager;
import samb.client.server.Server;
import samb.client.utils.UserData;
import samb.client.utils.inputs.Keyboard;
import samb.client.utils.inputs.Mouse;
import samb.com.database.UserInfo;
import samb.com.server.BaseProcessor;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;
import samb.com.server.packet.PacketFactory;
import samb.com.utils.Func;

public class Client extends BaseProcessor implements Runnable {
	/* This is the main Client class, when the program is executed the main method will be called and the Client will start.
	 * This is where all incoming DatagramPackets from the Host will be handled and directed to their respective area.
	 * */
	
	public static final double TPS = 60.0;
	private volatile boolean running = false;
	private Thread mainThread;
	
	public Server server;
	public PageManager pm;
	public UserData udata;
	
	private static Client thisClient;
	public static Window window;
	public static Mouse mouse;
	public static Keyboard keyboard;
	
	
	public Client() {
		Client.thisClient = this;
		
		this.server = new Server(this);
		Client.window = new Window(this);
		
		Func.loadFonts();
		
		this.pm = new PageManager();
		this.udata = new UserData();
		
		Client.mouse = new Mouse();
		Client.keyboard = new Keyboard();
		
		start();
	}
	
	private void tick() {
		// This method is called $Client.TPS$ per second
		// Any objects which require 'updating', ie balls moving, widget animations, etc
		//    will be called in this method

		pm.tick();
		
		Client.mouse.update();
		Window.updateBackground();
		super.process(); // BaseProcessor handles received packets
	}
	
	private void render() {
		// When rendering, the Window class creates a 'Graphics2D' object, which acts as a canvas to draw onto
		// Any object which requires rendering will render onto this Graphics2D object, which will then be rendered onto the window frame (ie the screen)
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
		//     ie login, signup, gameupadate, etc
		// the switch/case statement will direct each packet (using the header) to wherever it is needed
		
		LoginPage lp;
		GamePage gp;
		
		Packet p = PacketFactory.toPacket(packet.getData());
		switch(p.header) {
		case login:
			// This case receives from the host whether the user is authorized to continue
			if(p.loginInfo.authorized) {
				udata.id = p.id;
				udata.info = new UserInfo(p.id, p.loginInfo.username);
				pm.changePage(new MenuPage());
				
			} else {
				lp = (LoginPage) pm.get();
				lp.setError(p.loginInfo.err);
			}
			break;
			
		case signup:
			// This case receives from the host whether the user has been signed up
			if(p.loginInfo.authorized) {
				udata.id = p.id;
				udata.info = new UserInfo(p.id, p.loginInfo.username);
				pm.changePage(new MenuPage());
				
			} else {
				lp = (LoginPage) pm.get();
				lp.setError(p.loginInfo.err);
			}
			break;
			
			
		case newGame:
			// This case starts a new game 
			if(!pm.isId("GamePage") ) {
				pm.changePage(new GamePage());
			}

			gp = (GamePage) pm.get();
			gp.start(p.gameInfo);
			
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
			if(pm.isId("GamePage") ) {
				gp = (GamePage) pm.get();
				p.gameInfo = gp.getUpdate().gameInfo;
				
			} else {
				p.gameInfo = null;
			}
			
			server.send(p);
			break;
			
		case stopGame:
			// This case stops a game and returns the user back to the menu page
			if(!pm.isId("MenuPage")) {
				pm.changePage(new MenuPage());
			}
			break;
		
		case spectate:
			// This case informs the table of the current position of the spectated table
			if(!pm.isId("GamePage")) {
				pm.changePage(new GamePage());
			}

			gp = (GamePage) pm.get();
			gp.spectate(p.gameInfo);
			break;
			
			
		case getStats:
			// This case updates the udata of the users stats
			udata.stats = p.userStats;
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
		
//		int framerate = 0;
//		long timer = System.currentTimeMillis();
		
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
				//framerate++;
			}
			// Outputs the framerate
//			if(System.currentTimeMillis() - timer >= 1000) {
//				System.out.printf("Framerate %d\n", framerate);
//				framerate = 0;
//				timer = System.currentTimeMillis();
//			} 
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
	
	public static Client getClient() {
		// A little trick I learnt from reading the Minecraft source code was to set a static variable to the main class instance
		// Therefore, the main class instance does not need to be passed as a parameter but instead is returned by 'Client.getClient()'
		return thisClient;
		
	}
	
	public static void main(String[] args) {
		// This is the main function, called when the program starts, creating a new Client instance
		new Client();
		
	}

}
