package samb.client.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import samb.client.main.Client;
import samb.client.main.Window;
import samb.client.page.Page;
import samb.client.page.widget.ChatBox;
import samb.client.page.widget.GameMenu;
import samb.client.utils.ImageLoader;
import samb.com.server.info.GameInfo;
import samb.com.server.packet.Packet;

public class GamePage extends Page {
	/* This page is where the user will play/spectate their games
	 * This class handles the updating of the table
	 * */

	public GameInfo info;
	private Table table;
	private GameMenu menu;
	private ChatBox chat;
	
	private Client client;
	
	public GamePage(Client client) {
		super("GamePage");
		this.client = client;
		
		initWidgets();
		
	}
	
	private void initWidgets() {
		this.table = new Table(client, this);
		add("table", table);
		
		int buffer = 8;
		this.menu = new GameMenu(new int[] {3*Window.dim.width/4+buffer, buffer, 
				Window.dim.width/4-buffer*4, 2*Window.dim.height/3-buffer*4}, this);
		
		this.chat = new ChatBox(new int[] {3*Window.dim.width/4+buffer, 2*Window.dim.height/3-buffer*2, 
				Window.dim.width/4-buffer*4, Window.dim.height/3-buffer*4});
		add("chat", chat);
	}
	
	@Override
	public void tick() {
		menu.tick();
		tickWidgets();
		
		getRender();
		
	}
	
	public void startGame(GameInfo gi) {
		// This method starts a new game
		setGameInfo(gi);
		table.rack(gi);
		table.setUseCase(gi, client.udata.id);
		
	}
	
	public void updateTable(Packet p) {
		// This method updates the table, (when an opponent has hit the cue ball)
		table.update(p.updateInfo);
		
	}
	
	public void setGameInfo(GameInfo gi) {
		this.info = gi;
		this.info.opp = gi.u2.id.equals(client.udata.id) ? gi.u1.id : gi.u2.id;
		this.menu.setInfo(info);
		
	}
	
	public Packet getUpdate() {
		return table.createFullUpdate();
	}
	
	public void spectate(GameInfo gi) {
		startGame(gi); // Might have to change if required
		
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
