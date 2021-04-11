package samb.client.page;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import samb.client.game.Table;
import samb.client.main.Client;
import samb.client.main.Window;
import samb.client.page.widget.Button;
import samb.client.page.widget.ChatBox;
import samb.client.page.widget.EndScreen;
import samb.client.page.widget.GameMenu;
import samb.client.page.widget.Text;
import samb.client.page.widget.animations.BoxFocusAnimation;
import samb.client.page.widget.listeners.ButtonListener;
import samb.client.utils.ImageLoader;
import samb.client.utils.Maths;
import samb.com.database.UserInfo;
import samb.com.server.info.GameInfo;
import samb.com.server.info.GameState;
import samb.com.server.info.Message;
import samb.com.server.info.Win;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;
import samb.com.utils.Func;
import samb.com.utils.enums.TableUseCase;

public class GamePage extends Page implements ButtonListener {
	/* This page is where the user will play/spectate/practice their games
	 * The main 3 widgets are the Table, GameMenu and ChatBox
	 * */

	public GameInfo info;
	public GameState state;
	public boolean pooling = false;
	
	private Table table;
	private GameMenu menu;
	private ChatBox chat;
	private EndScreen endScreen;
	private Button retobut;
	
	public GamePage() {
		super("GamePage");
		
		initWidgets();
	}
	
	
	// Inits
	private void initWidgets() {
		// This method initializes the widgets displayed 
		this.table = new Table(this);
		add("table", table);
		
		int buffer = 8;
		this.menu = new GameMenu(new int[] {3*Window.dim.width/4+buffer, buffer, 
				Window.dim.width/4-buffer*4, Window.dim.height/3-buffer*4}, this);

		this.chat = new ChatBox(new int[] {3*Window.dim.width/4+buffer, Window.dim.height/3-buffer*2, 
				Window.dim.width/4-buffer*4, 2*Window.dim.height/3-buffer*4}, this);
		add("chat", chat);
		
		// Shown after a win/loss
		retobut = new Button(new int[] {3*Window.dim.width/8 -Window.dim.height/10, 
				Window.dim.height/2+Window.dim.height/24, 
				Window.dim.height/5, Window.dim.height/12}, "Return To Menu");
		retobut.id = "retobut";
		retobut.HIDDEN = true;
		retobut.addAnimation(new BoxFocusAnimation(retobut.rect));
		retobut.addListener(this);
		
		endScreen = new EndScreen(new int[] {3*Window.dim.width/8 - Window.dim.height/6, 
						Window.dim.height/3, Window.dim.height/3, Window.dim.height/3},
						retobut);
		endScreen.id = "endScreen";
		endScreen.HIDDEN = true;
		
	}
	
	
	// Tick method
	@Override
	public void tick() {
		menu.tick();
		
		tickWidgets();
		endScreen.tick();
		retobut.tick();
		
		getRender();
		
	}
	
	
	// Start methods
	public void start(GameInfo gi, GameState state) {
		// This method starts a new game
		this.pooling = false;
		menu.unshowLoading();
		
		setGame(gi, state);
		table.rack(gi); // table setup
		table.setUseCase(gi, Client.getClient().udata.id);
		table.setState(state);
		chat.setUseCase(gi.tuc);
		
		this.state = state;
		Client.getClient().udata.gameState = state;
		
	}
	
	public void spectate(GameInfo gi, GameState state) {
		start(gi, state); // Might have to change if required
		
	}
	
	
	public void pooling() {
		// Packet is sent to join the match-making pool
		Packet p = new Packet(Header.joinPool);
		Client.getClient().server.send(p);
		
		menu.unshowPlayers();
		menu.showPlayer1AsUsername();
		menu.showLoading();
		pooling = true;
		
	}
	
	public void unPool() {
		// Packet is sent to leave the match-making pool
		Packet p = new Packet(Header.leavePool);
		Client.getClient().server.send(p);
		pooling = false;
	}
	
	public void practice() {
		// Setup table for practicing
		GameInfo gi = new GameInfo("practice", null, null);
		gi.tuc = TableUseCase.practicing;
		gi.balls = Func.createDefaultBalls(gi.tDim);
		start(gi, new GameState());
		
	}
	
	
	// Update Methods
	public void updateTable(Packet p) {
		// This method updates the table, (when an opponent has hit the cue ball)
		table.update(p.updateInfo);
		
	}
	
	public void setGame(GameInfo gi, GameState state) {
		// This method sets the game's state (and info)
		if(gi != null) {
			this.info = gi;
			Client.getClient().udata.gameInfo = gi;
			Client.getClient().udata.gameState = state;
			menu.setInfo(info);
			
		}
	}
	
	
	public Packet getUpdate() {
		// Returns a packet of all the info required to send a copy/update of the game
		Packet p = new Packet(Header.updateGame);
		p.gameInfo = info;
		p.gameInfo.balls = table.getCircles();
		p.gameState = state;
		
		return p;
	}
	
	public void endGame(Win win, String winnerId) {
		// This method starts to reveal the endScreen
		endScreen.tuc = table.tuc;
		switch(table.tuc) {
		case playing:
			endScreen.setDeltaElo(getEloDelta(winnerId));
			endScreen.reveal(win, Client.getClient().udata.id.equals(winnerId));
			break;
			
		case practicing:
			endScreen.setDeltaElo(0);
			endScreen.reveal(win, true);
			break;
			
		case spectating:
			endScreen.setDeltaElo(getRawEloDelta(winnerId));
			endScreen.reveal(win, info.u1, info.u2, winnerId);
			break;
		}
		
		retobut.HIDDEN = false;
	}
	
	private int getRawEloDelta(String wId) {
		UserInfo loser = wId.equals(info.u1.id) ? info.u2 : info.u1;
		int diffelo = loser.elo - getUI(wId).elo;
		return Maths.calculateDeltaElo(diffelo);
	}
	
	private int getEloDelta(String wId) {
		UserInfo loser = wId.equals(info.u1.id) ? info.u2 : info.u1;
		int diffelo = loser.elo - getUI(wId).elo;
		if(Client.getClient().udata.id.equals(wId)) {
			return Maths.calculateDeltaElo(diffelo); 
		} else {
			return -Maths.calculateDeltaElo(diffelo);
		}
	}
	
	
	// Return To Menu button listener methods
	@Override
	public void onClick(Button b) {
		if(b.id.equals("retobut")) { // Return to Menu button on endScreen
			Client.getClient().pm.changePage(new MenuPage());
			
		} else if(b.id.equals("forbut")) { // Forfeit button on gameMenu Widget
			if(table.tuc == TableUseCase.playing) {
				if(pooling) {
					unPool();
					Client.getClient().pm.changePage(new MenuPage());
				} else {
					table.win(getNotTurnID(), Win.forfeit);
				}
				
			} else { // if you are spectating/practicing, return to menu
				Client.getClient().pm.changePage(new MenuPage());
			}
		}
	}

	@Override
	public void onRelease(Button b) {}
	
	
	// Chat Methods
	public void addChat(Message msg) {
		chat.addMessage(msg);
		
	}
	
	
	// Getters (sort of) 
	public String getTurnID() {
		if(table.tuc == TableUseCase.practicing) { return Client.getClient().udata.id; }
		if(info.first) {
			return table.turn ? info.u1.id : info.u2.id;
		} else {
			return table.turn ? info.u2.id : info.u1.id;
		}
	}
	
	public String getNotTurnID() {
		if(table.tuc == TableUseCase.practicing) { return Client.getClient().udata.id; }
		if(info.first) {
			return table.turn ? info.u2.id : info.u1.id;
		} else {
			return table.turn ? info.u1.id : info.u2.id;
		}
	}
	
	public String getTurnName() {
		if(table.tuc == TableUseCase.practicing) { return "You"; }
		if(info.first) {
			return table.turn ? info.u1.username : info.u2.username;
		} else {
			return table.turn ? info.u2.username : info.u1.username;
		}
	}
	
	public String getNotTurnName() {
		if(table.tuc == TableUseCase.practicing) { return "You"; }
		if(info.first) {
			return table.turn ? info.u2.username : info.u1.username;
		} else {
			return table.turn ? info.u1.username : info.u2.username;
		}
	}
	
	public UserInfo getUI(String id) {
		return info.u1.id.equals(id) ? info.u1 : info.u2;
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
		
		endScreen.render(g);
		
		return img;
	
	}

}
