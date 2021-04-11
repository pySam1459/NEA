package samb.client.page;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import samb.client.main.Client;
import samb.client.main.Window;
import samb.client.page.widget.Button;
import samb.client.page.widget.FriendList;
import samb.client.page.widget.FriendProfile;
import samb.client.page.widget.Text;
import samb.client.page.widget.TextBox;
import samb.client.page.widget.animations.BoxFocusAnimation;
import samb.client.page.widget.animations.HoverShineAnimation;
import samb.client.page.widget.animations.UnderLineAnimation;
import samb.client.page.widget.listeners.ButtonListener;
import samb.client.utils.Consts;
import samb.client.utils.ImageLoader;
import samb.com.database.UserStats;
import samb.com.server.info.ChallengeInfo;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;

public class MenuPage extends Page implements ButtonListener {
	/* This subclass represents the menu which the user will use to join the pool, invite a user to play, spectate a game,
	 * or practice on a offline table. Other sub-options include: profile, settings and stats
	 * */
	
	public FriendProfile fp;
	private FriendList fl;
	
	public MenuPage() {
		super("MenuPage");
		
		initWidgets();
		requestStats();
		
	}
	
	private void initWidgets() {
		// This method initializes widgets
		
		int buffer = 32;
		Text unTitle = new Text(Client.getClient().udata.userInfo.username, 
				new int[] {buffer*5, 64, Window.dim.width/3, 100}, Consts.INTER.deriveFont(Font.PLAIN, 96), Consts.PAL1);
		unTitle.CENTERED = false;
		add("unTitle", unTitle);
		
		int utw = unTitle.getRight();
		Text eloTitle = new Text("~", new int[] {utw + buffer, 124, Window.dim.width/3-utw, 24}, 
				Consts.INTER.deriveFont(Font.PLAIN, 24), Consts.PAL1);
		eloTitle.CENTERED = false;
		add("eloTitle", eloTitle);
		
		int butW = 384, butH = 129, yoff=156;
		Color backColor = new Color(24, 231, 204, 128);
		Button but = new Button(new int[] {buffer*5, yoff+buffer*2, butW, butH}, "Join Pool");
		but.addAnimation(new HoverShineAnimation(but.rect));
		but.addAnimation(new BoxFocusAnimation(but.rect));
		but.BACKGROUND_COLOR = backColor;
		but.addListener(this);
		add("jpButton", but);
		
		but = new Button(new int[] {buffer*6 + butW, yoff+buffer*2, butW, butH}, "Practice");
		but.addAnimation(new HoverShineAnimation(but.rect));
		but.addAnimation(new BoxFocusAnimation(but.rect));
		but.BACKGROUND_COLOR = backColor;
		but.addListener(this);
		add("pracButton", but);
		
		fp = new FriendProfile(new int[] {buffer*5, yoff+butH+buffer*4, 
				butW*2 + buffer, Window.dim.height-(yoff+butH+buffer*9 +4)}, this);
		fp.HIDDEN = true;
		

		fl = new FriendList(new int[] {buffer*9+2*butW, yoff, 
				Window.dim.width-2*butW-13*buffer, Window.dim.height-10*buffer},
				fp);
		add("friendList", fl);
		
		int h = 64;
		TextBox search = new TextBox(new int[] {buffer*9+2*butW+8, yoff-buffer-h, 
				Window.dim.width-2*butW-15*buffer-h, h}, "Search...");
		search.charLimit = 20;
		search.round = false;
		search.underline = true;
		search.addAnimation(new UnderLineAnimation(search.rect));
		search.addListener(fl);
		add("searchBox", search);
		
		but = new Button(new int[] {Window.dim.width-5*buffer-h+8, yoff-buffer-h, h, h}, "R");
		but.addListener(this);
		add("refButton", but);
		
	}
	
	@Override
	public void onClick(Button b) {
		// This method listeners for any button clicks on this page
		GamePage gp;
		switch(b.id) {
		case "jpButton":
			gp = new GamePage();
			Client.getClient().pm.changePage(gp);
			gp.pooling();
			break;
			
		case "pracButton":
			gp = new GamePage();
			Client.getClient().pm.changePage(gp);
			gp.practice();
			break;
			
		case "refButton":
			fl.getFriends();
			break;
			
		default:
			System.out.println("Unknown button " + b.id);
		
		}
	}
	
	private void requestStats() {
		// Requests statistics from host
		Packet p = new Packet(Header.getStats);
		Client.getClient().server.send(p);	
	}
	
	public void setStats(UserStats us) {
		((Text)get("eloTitle")).setText(Integer.toString(us.elo));
		
	}
	
	public void recvChallenge(ChallengeInfo ci) {
		fl.recvChallenge(ci);
		
	}
	

	@Override
	public void tick() {
		tickWidgets();
		fp.tick();
		getRender();
		
	}

	@Override
	public BufferedImage getRender() {
		Graphics2D g = getBlankCanvas();
		g.drawImage(ImageLoader.getBackground(), 0, 0, Window.dim.width, Window.dim.height, null);
		
		fp.render(g);
		renderWidgets(g);
		
		return img;
	}


	@Override
	public void onRelease(Button b) {}

}
