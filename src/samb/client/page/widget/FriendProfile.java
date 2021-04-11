package samb.client.page.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import samb.client.main.Client;
import samb.client.page.MenuPage;
import samb.client.page.widget.animations.BoxFocusAnimation;
import samb.client.page.widget.animations.UnderLineAnimation;
import samb.client.page.widget.listeners.ButtonListener;
import samb.client.utils.Consts;
import samb.com.database.Friend;
import samb.com.database.UserStats;
import samb.com.server.info.ChallengeInfo;
import samb.com.server.info.FriendsInfo;
import samb.com.server.packet.FHeader;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;

public class FriendProfile extends Widget implements ButtonListener {
	/* This Widget subclass displays information about a selected friend, 
	 *   from the friendsList widget on the MenuPage
	 * From this widget, a user will be able to challenge or spectate their friend
	 * */
	
	private final int buffer=8, usernameH;
	private final BasicStroke borderStroke = new BasicStroke(3);
	
	private BufferedImage img;
	private Font usernameFont;
	
	private MenuPage mp;
	private Button challBut, acChallBut, specBut, addBut;
	private Text challInfo;
	
	private HashMap<String, UserStats> statsCache;
	public Friend f;
	public ChallengeInfo ci;
	private UserStats stats;

	public FriendProfile(int[] rect, MenuPage mp) {
		super(rect);
		this.mp = mp;
		
		this.usernameH = rect[3]/5;
		this.usernameFont = Consts.INTER.deriveFont(Font.PLAIN, usernameH);
		
		this.statsCache = new HashMap<>();
		
		initWidgets();
	}
	
	public void set(Friend f) {
		// Sets the profile to Friend f
		this.f = f;
		setBools(f);
		getStats(f.id);
		createImage();
	}
	
	private void getStats(String id) {
		// Requests the stats of a user with $id (or gets from cache)
		
		if(statsCache.containsKey(id)) { // use of cache, reduce number of requests
			setStats(statsCache.get(id), false);
			
		} else { // requests id's stats
			Packet p = new Packet(Header.getStats);
			p.friendsInfo = new FriendsInfo(FHeader.getStats, id);
			Client.getClient().server.send(p);
			this.stats = null;
		}
	}
	
	// Setters
	public void setStats(UserStats fus, boolean reImage) {
		// Sets the stats on the profile
		
		if(!statsCache.containsKey(fus.id)) {
			statsCache.put(fus.id, fus); // cache stats
		}
		
		this.stats = fus;
		
		if(reImage) {
			createImage();
		}
	}
	
	public void setBools(Friend f) {
		challBut.active = f.online && !f.inGame;
		specBut.active = f.inGame;
		
	}
	
	public void setHidden(boolean hidden) {
		if(hidden) { // hide all
			challBut.HIDDEN = true;
			acChallBut.HIDDEN = true;
			specBut.HIDDEN = true;
			addBut.HIDDEN = true;
						
		} else {
			if(f.isFriend) { // challenge and invite buttons are shown
				if(f.challenged) { // accept challenge button
					challBut.HIDDEN = true;
					acChallBut.HIDDEN = false;
					specBut.HIDDEN = false;
					addBut.HIDDEN = true;
					
				} else { // challenge button
					challBut.HIDDEN = false;
					acChallBut.HIDDEN = true;
					specBut.HIDDEN = false;
					addBut.HIDDEN = true;
				}
				
			} else { // add friend button is shown
				challBut.HIDDEN = true;
				acChallBut.HIDDEN = true;
				specBut.HIDDEN = true;
				addBut.HIDDEN = false;
			}
		}
		challInfo.HIDDEN = true;
		this.HIDDEN = hidden;
	}
	
	
	@Override
	public void tick() {
		super.animTick();

	}
	
	private void initWidgets() {
		int w = rect[2]/3, h = rect[3]/8;
		
		challBut = new Button(new int[] {rect[0]+rect[2]/2-buffer-w, 
				rect[1]+rect[3]-buffer*3-h, w, h}, "Challenge");
		challBut.HIDDEN = true;
		challBut.active = false;
		challBut.addAnimation(new UnderLineAnimation(challBut.rect));
		challBut.addListener(this);
		mp.add("challBut", challBut);
		
		acChallBut = new Button(new int[] {rect[0]+rect[2]/2-buffer-w, 
				rect[1]+rect[3]-buffer*3-h, w, h}, "Accept Challenge");
		acChallBut.HIDDEN = true;
		acChallBut.active = true;
		acChallBut.addAnimation(new UnderLineAnimation(acChallBut.rect));
		acChallBut.addListener(this);
		mp.add("acChallBut", acChallBut);
		
		challInfo = new Text("", new int[] {rect[0]+rect[2]/2-buffer-3*w/2, 
				(int)(rect[1]+rect[3]-buffer*4-h*1.4), w*2, (int)(h*0.4)}, 
				new Font("comicsansms", Font.PLAIN, (int)(h*0.4)), Consts.PALE);
		challInfo.HIDDEN = true;
		mp.add("challInfo", challInfo);
		
		specBut = new Button(new int[] {rect[0]+rect[2]/2+buffer, 
				rect[1]+rect[3]-buffer*3-h, w, h}, "Spectate");
		specBut.HIDDEN = true;
		specBut.active = false;
		specBut.addAnimation(new UnderLineAnimation(specBut.rect));
		specBut.addListener(this);
		mp.add("specBut", specBut);
		
		addBut = new Button(new int[] {rect[0]+rect[2]/2-w/2, 
				rect[1]+rect[3]-buffer*3-h, w, h}, "Add Friend");
		addBut.HIDDEN = true;
		addBut.addAnimation(new BoxFocusAnimation(addBut.rect));
		addBut.addListener(this);
		mp.add("addBut", addBut);
		
	}
	
	@Override
	public void onClick(Button b) {
		Packet p;
		switch(b.id) {
		case "challBut":
			p = new Packet(Header.challenge);
			p.challengeInfo = new ChallengeInfo(f.id);
			Client.getClient().server.send(p);
			
			challInfo.setText("Challenge Sent");
			challInfo.setColour(Consts.PALE);
			challInfo.HIDDEN = false;
			break;
			
		case "acChallBut":
			p = new Packet(Header.challenge);
			f.ci.accepted = true;
			p.challengeInfo = f.ci;
			Client.getClient().server.send(p);
			f.challenged = false;
			break;
			
		case "specBut":
			p = new Packet(Header.spectate);
			p.spec = f.id;
			Client.getClient().server.send(p);
			break;
			
		case "addBut":
			p = new Packet(Header.addFriend);
			p.friendsInfo = new FriendsInfo(FHeader.addFriend, f.id);
			Client.getClient().server.send(p);
			f.isFriend = true;
			setHidden(false);
			break;
			
		default:
			break;
		}
	}

	@Override
	public void onRelease(Button b) {}
	
	public void challengeError(Error err) {
		challInfo.setText("Error: " + err.toString());
		challInfo.setColour(Color.RED);
		challInfo.HIDDEN = false;
	}
	

	// Render Methods
	@Override
	public void render(Graphics2D g) {
		// Renders the widget
		
		if(!HIDDEN) {
			if(img != null) {
				g.drawImage(img, rect[0], rect[1], null);
			}
			
			super.animRender(g);
		}
	}
	
	private void createImage() {
		// Creates the Profile Image, to be rendered
		
		this.img = new BufferedImage(rect[2], rect[3], BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		g.setColor(Consts.MENU_BACKGROUND_COLOR);
		g.fillRoundRect(buffer, buffer, rect[2]-buffer*2, rect[3]-buffer*2, buffer*2, buffer*2);
		
		// Username
		TextInfo ti = new TextInfo(f.username, usernameFont, Consts.PALE);
		Point xy = new Point(buffer*5, buffer*2+usernameH);
		ti.render(g, xy);
		
		// Displaying stats
		String[] titles = new String[] {"Elo", "Max Elo", "Win %", "Games", "Wins", "Pots"};
		String[] values = new String[] {"~", "~", "~", "~", "~", "~"};
		if(stats != null) {
			values = getDisplayedStats();
		}
		
		int h = rect[3]/14;
		int x = buffer*5;
		int y = buffer*6 + usernameH+h;
		ti.setSize(h);
		for(int i=0; i<2; i++) {
			for(int j=0; j<3; j++) {
				// Title
				ti.setColour(Consts.GREY_PALE);
				ti.setText(titles[i*3 + j]);
				ti.render(g, new Point(x, y+j*(h+buffer)));
				int w = ti.dim.width;
				
				// Value
				ti.setColour(Consts.PALE);
				ti.setText(values[i*3+j]);
				ti.render(g, new Point(x+w+buffer*2, y+j*(h+buffer)));
				
			}
			// After 1 column
			x = rect[2]/2 + buffer;
		}
		
		g.setStroke(borderStroke);
		g.setColor(Consts.MENU_BORDER_COLOR);
		g.drawRoundRect(buffer, buffer, rect[2]-buffer*2, rect[3]-buffer*2, buffer*2, buffer*2);
	}
	
	
	private String[] getDisplayedStats() {
		// Returns a String[] {elo, max elo, win %, # games, # wins, # balls potted}
		int winperc = (int)(((double)stats.noGamesWon / (double)stats.noGames)*100.0);
		return new String[] {Integer.toString(stats.elo), Integer.toString(stats.highestElo),
				Integer.toString(winperc), Integer.toString(stats.noGames),
				Integer.toString(stats.noGamesWon), Integer.toString(stats.noBallsPotted)};
	}

}
