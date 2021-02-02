package samb.client.page;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import samb.client.game.GamePage;
import samb.client.main.Client;
import samb.client.main.Window;
import samb.client.page.widget.Button;
import samb.client.page.widget.ButtonListener;
import samb.client.page.widget.Text;
import samb.client.page.widget.animations.HoverShineAnimation;
import samb.client.utils.Consts;
import samb.client.utils.ImageLoader;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;

public class MenuPage extends Page implements ButtonListener {
	/* This subclass represents the menu which the user will use to join the pool, invite a user to play, spectate a game,
	 * or practice on a offline table. Other sub-options include: profile, settings and stats
	 * */
	
	public MenuPage() {
		super("MenuPage");
		
		initWidgets();
		requestStats();
		
	}
	
	private void initWidgets() {
		int buffer = 32;
		Text unTitle = new Text(Client.getClient().udata.info.username, new int[] {buffer*5, 64, Window.dim.width/2, 100}, Consts.INTER.deriveFont(Font.PLAIN, 96), Consts.PAL1);
		unTitle.CENTERED = false;
		add("unTitle", unTitle);
		
		int butW = 384, butH = 129, yoff=156;
		Color backColor = new Color(24, 231, 204, 128);
		Button but;
		but = new Button(new int[] {buffer*5, yoff + butH, butW, butH}, "Join Pool", null);
		but.addAnimation(new HoverShineAnimation(but.rect));
		but.BACKGROUND_COLOR = backColor;
		but.addListener(this);
		add("jpButton", but);
		
		but = new Button(new int[] {buffer*6 + butW, yoff + butH, butW, butH}, "Spectate", null);
		but.addAnimation(new HoverShineAnimation(but.rect));
		but.BACKGROUND_COLOR = backColor;
		but.addListener(this);
		add("specButton", but);
		
		but = new Button(new int[] {buffer*5, yoff+butH*2 + buffer, butW, butH}, "Invite Player", null);
		but.addAnimation(new HoverShineAnimation(but.rect));
		but.BACKGROUND_COLOR = backColor;
		but.addListener(this);
		add("invButton", but);
		
		but = new Button(new int[] {buffer*6 + butW, yoff+butH*2 + buffer, butW, butH}, "Practice", null);
		but.addAnimation(new HoverShineAnimation(but.rect));
		but.BACKGROUND_COLOR = backColor;
		but.addListener(this);
		add("pracButton", but);
		
	}
	
	@Override
	public void onClick(Button b) {
		GamePage gp;
		switch(b.id) {
		case "jpButton":
			break;
			
		case "specButton":
			break;
			
		case "invButton":
			break;
			
		case "pracButton":
			gp = new GamePage();
			Client.getClient().pm.changePage(gp);
			gp.practice();
			break;
			
		default:
			System.out.println("Unknown button " + b.id);
		
		}
	}
	
	private void requestStats() {
		Packet p = new Packet(Header.getStats);
		Client.getClient().server.send(p);
		
	}

	@Override
	public void tick() {
		tickWidgets();
		getRender();
		
	}

	@Override
	public BufferedImage getRender() {
		Graphics2D g = getBlankCanvas();
		g.drawImage(ImageLoader.getBackground(), 0, 0, Window.dim.width, Window.dim.height, null);
		
		renderWidgets(g);
		
		return img;
	}


	@Override
	public void onRelease(Button b) {}

}
