package samb.client.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import samb.client.main.Client;
import samb.client.main.Window;
import samb.client.page.Page;
import samb.client.utils.ImageLoader;
import samb.com.server.packet.GameInfo;
import samb.com.server.packet.Packet;

public class GamePage extends Page {
	/* This page is where the user will play/spectate their games
	 * This class handles the updating of the table
	 * */

	public GameInfo info;
	private Table table;
	private Client client;
	
	public GamePage(Client client) {
		super("GamePage");
		this.client = client;
		
		this.table = new Table(this);
		add("table", table);
		
	}
	
	@Override
	public void tick() {
		tickWidgets();
		getRender();
		
	}
	
	public void startGame(GameInfo gi) {
		// This method starts a new game
		setGameInfo(gi);
		table.spectating = !(gi.u1.id.equals(client.udata.id) || gi.u2.id.equals(client.udata.id));
		if(!table.spectating) {
			table.turn = gi.u1.id.equals(client.udata.id);
		}
	}
	
	public void updateTable(Packet p) {
		// This method updates the table, (when an opponent has hit the cue ball)
		table.update(p.gameInfo);
		
	}
	
	public void setGameInfo(GameInfo gi) {
		this.info = gi;
		table.update(gi);
		
	}
	
	public Packet getUpdate() {
		return table.getUpdate();
	}
	
	
	// Rendering
	@Override
	public BufferedImage getRender() {
		Graphics2D g = getBlankCanvas();
		g.drawImage(ImageLoader.getBackground(), 0, 0, Window.dim.width, Window.dim.height, null);
		
		g.setColor(new Color(255, 0, 0, 200));
		g.fillRect(3*Window.dim.width/4, 0, Window.dim.width/4, Window.dim.height);
		
		renderWidgets(g);
		
		return img;
	
	}

}
