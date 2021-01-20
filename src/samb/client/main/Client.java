package samb.client.main;

import java.awt.Graphics2D;
import java.net.DatagramPacket;

import samb.client.game.GamePage;
import samb.client.page.LoginPage;
import samb.client.page.MenuPage;
import samb.client.page.PageManager;
import samb.client.server.Server;
import samb.client.utils.Keyboard;
import samb.client.utils.Mouse;
import samb.client.utils.UserData;
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
	private PageManager pm;
	public UserData udata;
	
	public static Window window;
	public static Mouse mouse;
	public static Keyboard keyboard;
	
	
	public Client() {
		this.server = new Server(this);
		Client.window = new Window(this); // TODO might cause problems in the future
		
		Func.loadFonts();
		
		this.pm = new PageManager(this);
		this.udata = new UserData();
		
		Client.mouse = new Mouse();
		Client.keyboard = new Keyboard();
		
		start();
	}
	
	private void tick() {
		// This method calls an objects which require 'updating', ie balls moving, widget animations, etc
		// This method is called $Client.TPS$ per second
		
		pm.tick();
		
		Client.mouse.update();
		Window.updateBackground();
		super.process();
	}
	
	private void render() {
		// When rendering, the window's class creates a 'Graphics2D' object which acts as a canvas to draw onto
		// Any object which requires rendering will render onto this Graphics2D object, which will then be rendered onto the window frame
		// Order of rendering is very important as objects which are rendered first will be drawn over by later objects
		
		Graphics2D g = window.getGraphics();  // Get 'canvas' to draw on
		
		pm.render(g); // Renders the page being show, or a transition
		
		window.render();  // Show 'painted canvas' to screen
		
	}
	
	
	@Override
	public void handle(DatagramPacket packet) {
		// This methods handles incoming DatagramPackets from the Host Server
		// A DatagramPacket contains a Packet object which has a header
		// This header is used to identify the purpose of the packet, ie login, signup, gameupadate, etc
		
		LoginPage lp;
		GamePage gp;
		
		Packet p = PacketFactory.toPacket(packet.getData());
		switch(p.header) {
		case login:
			// This case receives from the host whether the user is authorized to continue
			if(p.loginInfo.authorized) {
				udata.id = p.id;
				udata.info = new UserInfo(p.id, p.loginInfo.username);
				pm.changePage(new MenuPage(this));
				
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
				pm.changePage(new MenuPage(this));
				
			} else {
				lp = (LoginPage) pm.get();
				lp.setError(p.loginInfo.err);
			}
			break;
			
			
		case newGame:
			// This case starts a new game 
			if(!pm.get().id.equals("GamePage")) {
				pm.changePage(new GamePage(this));
			}
			gp = (GamePage) pm.get();
			gp.startGame(p.gameInfo);
			
			break;
			
		case updateGame:
			// This case updates the on going game with the latest move
			if(!pm.get().id.equals("GamePage")) {
				pm.changePage(new GamePage(this));
			}
			gp = (GamePage) pm.get();
			gp.updateTable(p);
			break;
			
		case getUpdateGame:
			// The case sends the current state of the table to the server (used for new spectators)
			if(!pm.get().id.equals("GamePage")) {
				p.gameInfo = null;
				
			} else {
				gp = (GamePage) pm.get();
				p = gp.getUpdate();
			}
			server.send(p);
			break;
			
		case stopGame:
			pm.changePage(new MenuPage(this));
			break;
			
			
		case getStats:
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
	
	public static void main(String[] args) {
		new Client();
		
	}

}
