package samb.client.game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import samb.client.main.Client;
import samb.client.main.Window;
import samb.client.page.Page;
import samb.client.page.widget.ChatBox;
import samb.client.page.widget.GameMenu;
import samb.client.page.widget.Text;
import samb.client.utils.ImageLoader;
import samb.com.server.info.GameInfo;
import samb.com.server.info.GameState;
import samb.com.server.info.Message;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;
import samb.com.utils.Func;
import samb.com.utils.enums.TableUseCase;

public class GamePage extends Page {
	/* This page is where the user will play/spectate/practice their games
	 * The main 3 widgets are the Table, GameMenu and ChatBox
	 * */

	public GameInfo info;
	public GameState state;
	private Table table;
	private GameMenu menu;
	private ChatBox chat;
	
	private Client client;
	
	public GamePage() {
		super("GamePage");
		this.client = Client.getClient();
		
		initWidgets();
		
	}
	
	
	// Inits
	private void initWidgets() {
		this.table = new Table(client, this);
		add("table", table);
		
		int buffer = 8;
		this.menu = new GameMenu(new int[] {3*Window.dim.width/4+buffer, buffer, 
				Window.dim.width/4-buffer*4, Window.dim.height/2-buffer*4}, this);

		this.chat = new ChatBox(new int[] {3*Window.dim.width/4+buffer, Window.dim.height/2-buffer*2, 
				Window.dim.width/4-buffer*4, Window.dim.height/2-buffer*4}, this);
		add("chat", chat);
	}
	
	
	// Tick method
	@Override
	public void tick() {
		menu.tick();
		tickWidgets();
		
		getRender();
		
	}
	
	
	// Start methods
	public void start(GameInfo gi, GameState state) {
		// This method starts a new game
		menu.unshowLoading();
		
		setGameInfo(gi);
		table.rack(gi);
		table.setUseCase(gi, client.udata.id);
		chat.setUseCase(gi.tuc);
		
		this.state = state;
		Client.getClient().udata.gameState = state;
		
	}
	
	public void spectate(GameInfo gi, GameState state) {
		start(gi, state); // Might have to change if required
		
	}
	
	
	public void pooling() {
		Packet p = new Packet(Header.joinPool);
		client.server.send(p);
		
		menu.unshowPlayers();
		menu.showPlayer1AsUsername();
		menu.showLoading();
		
	}
	
	public void practice() {
		GameInfo gi = new GameInfo("practice", null, null);
		gi.tuc = TableUseCase.practicing;
		gi.balls = Func.createDefaultBalls(gi.tDim, Ball.DEFAULT_BALL_RADIUS);
		start(gi, new GameState());
		
	}
	
	
	// Update Methods
	public void updateTable(Packet p) {
		// This method updates the table, (when an opponent has hit the cue ball)
		table.update(p.updateInfo);
		
	}
	
	public void setGameInfo(GameInfo gi) {
		if(gi != null) {
			this.info = gi;
			Client.getClient().udata.gameInfo = gi;
					
			if(gi.tuc == TableUseCase.playing) {
				this.info.opp = gi.u2.id.equals(client.udata.id) ? gi.u1.id : gi.u2.id;
			}
			
			menu.setInfo(info);
		}
	}
	
	
	public Packet getUpdate() {
		Packet p = new Packet(Header.updateGame);
		p.gameInfo = info;
		p.gameInfo.balls = table.getCircles();
		p.gameInfo.turn = table.turn ? info.id : info.opp;
		
		return p;
	}
	
	
	// Chat Methods
	public void addChat(Message msg) {
		chat.addMessage(msg);
		
	}
	
	
	// Getters (sort of) 
	public String getTurnID() {
		if(info.first) {
			return table.turn ? info.u1.id : info.u2.id;
		} else {
			return table.turn ? info.u2.id : info.u1.id;
		}
	}
	
	public String getNotTurnID() {
		if(info.first) {
			return table.turn ? info.u2.id : info.u1.id;
		} else {
			return table.turn ? info.u1.id : info.u2.id;
		}
	}
	
	public String getTurnName() {
		if(info.first) {
			return table.turn ? info.u1.username : info.u2.username;
		} else {
			return table.turn ? info.u2.username : info.u1.username;
		}
	}
	
	
	// Setters (sort of)
	public void setMenuTitleColours() {
		if(Client.getClient().udata.id.equals(state.redID)) {
			((Text)this.get("title1")).setColour(GameMenu.RED);
			((Text)this.get("title2")).setColour(GameMenu.YELLOW);
			
		} else {
			((Text)this.get("title1")).setColour(GameMenu.YELLOW);
			((Text)this.get("title2")).setColour(GameMenu.RED);
			
		}
	}
	
	
	// Rendering
	@Override
	public BufferedImage getRender() {
		Graphics2D g = getBlankCanvas();
		g.drawImage(ImageLoader.getBackground(), 0, 0, Window.dim.width, Window.dim.height, null);
		
		menu.render(g);
		renderWidgets(g);
		
		return img;
	
	}

}
