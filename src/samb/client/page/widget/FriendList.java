package samb.client.page.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.List;

import samb.client.main.Client;
import samb.client.page.widget.animations.BoxFocusAnimation;
import samb.client.utils.Consts;
import samb.client.utils.Maths;
import samb.com.database.Friend;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;

public class FriendList extends Widget implements MouseWheelListener {
	/* This widget displays a list of all your friends
	 * */
	
	private final int scrollSpeed=16, buffer=8;
	private int scroll = 0, highlight=-1, selected=-1;

	private int fH = 56+buffer*2, fW;
	private List<Friend> friends;
	
	private Font friendNameFont;
	private final BasicStroke borderStroke = new BasicStroke(3), highlightStroke = new BasicStroke(2);
	private final Color BACKGROUND_COLOUR = new Color(0, 151, 162, 178);
	private Point xy;
	
	private BoxFocusAnimation anim;
	private BufferedImage img, section;
	private FriendProfile fp;
	
	public FriendList(int[] rect, FriendProfile fp) {
		super(rect);
		this.fW = (rect[2]-buffer*8)/2;
		this.fp = fp;
		
		this.friendNameFont = Consts.INTER.deriveFont(Font.PLAIN, fH/1.8f);
		getFriends();
		
		this.anim = new BoxFocusAnimation(new int[] {-100,-100,1,1}, true);
		addAnimation(anim);
		
		Client.getWindow().addMouseWheelListener(this);
	}
	
	@Override
	public void tick() {
		getHighlight();
		select();
		super.animTick();
		
	}
	
	private void getHighlight() {
		// This method gets the index of the friend the mouse if over
		
		xy = Client.getMouse().getXY();
		if(Maths.pointInRect(xy, rect)) {
			int index = 2*(int)((scroll + xy.y - rect[1]-buffer*3) / fH);
			if(xy.x > rect[0]+rect[2]/2) {
				index++;
			}if(0 <= index && index < friends.size() && buffer*3+(int)(index/2)*fH -scroll < rect[3] && highlight != index) {
				this.highlight = index;
				setAnimRect();
			}
			
		} else {
			this.highlight = -1;
		}
	}
	
	private void select() {
		if(Client.getMouse().left && Client.getMouse().forleft < 2) {
			if(highlight != -1) {
				this.selected = highlight;
				fp.set(friends.get(selected));
				fp.setHidden(false);
			} else if((Maths.pointInRect(xy, fp.rect) && fp.HIDDEN) || !Maths.pointInRect(xy, fp.rect)) {
				this.selected = -1;
				fp.setHidden(true);
			}
		}		
	}
	
	private void getFriends() { // Requests the friends list
		Packet p = new Packet(Header.getFriends);
		Client.getClient().server.send(p);
	}
	
	public void setFriends(Packet p) { // Sets the friends list
		this.friends = p.friendsInfo.friends;
		renderFriends();
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent m) {
		// This method is called by an interface when the mouse wheel is scrolled
		Point xy = Client.getMouse().getXY();
		if(Maths.pointInRect(xy, rect)) {
			int maxS = (int)(friends.size()/2)*fH + buffer*3;
			if(maxS < rect[3]) {
				this.scroll = 0;
				return;
			}
			// calculates the new scroll offset
			int newscroll = scroll + scrollSpeed * m.getWheelRotation();
			
			if(newscroll < 0 || maxS < rect[3]) {
				this.scroll = 0;
			} else if(newscroll+rect[3] > maxS) {
				this.scroll = maxS - rect[3];
			} else {
				this.scroll = newscroll;
			}
			setAnimRect();
			
		}
	}
	
	private void setAnimRect() {
		this.anim.setRect(new int[] {rect[0]+buffer*3+(fW+buffer*2)*(highlight%2),
									 rect[1]+buffer*3+(int)(highlight/2)*fH-scroll, fW, fH-buffer*2});
	}

	@Override
	public void render(Graphics2D graph) {
		// Renders the friendsList
		
		BufferedImage img = new BufferedImage(rect[2], rect[3], BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		g.setColor(Consts.MENU_BACKGROUND_COLOR);
		g.fillRoundRect(buffer, buffer, rect[2]-buffer*2, rect[3]-buffer*2, buffer*2, buffer*2);

		renderImg(g);
		
		g.setStroke(borderStroke);
		g.setColor(Consts.MENU_BORDER_COLOR);
		g.drawRoundRect(buffer, buffer, rect[2]-buffer*2, rect[3]-buffer*2, buffer*2, buffer*2);
		
		graph.drawImage(img, rect[0], rect[1], null);
		super.animRender(graph);
	}

	private void renderImg(Graphics2D g) {
		// Renders the image, and the highlighted friend on top
		
		if(img != null) {
			section = img.getSubimage(0, scroll, rect[2], rect[3]-buffer*2);
			g.drawImage(section, 0, buffer, null);
			
//			if(highlight != -1 && selected != highlight) {
//				highlightIndex(g, highlight, Consts.PALE);
//			} 
			if(selected != -1) {
				highlightIndex(g, selected, new Color(245, 245, 245, 64));
			}
		}
	}
	
	private void highlightIndex(Graphics2D g, int index, Color colour) {
		g.setColor(colour);
		g.setStroke(highlightStroke);
		g.fillRoundRect(buffer*3+(fW+buffer*2)*(index%2), 
				buffer*3+(int)(index/2)*fH-scroll, fW, fH-buffer*2, buffer*2, buffer*2);
	}
	
	private void renderFriends() {
		// Creates an image of the friendsList widget, only called once
		
		int h = Math.max(rect[3], (int)(friends.size())*fH) + buffer*3;
		this.img = new BufferedImage(rect[2], h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		g.setFont(friendNameFont);
		int i = 0;
		for(Friend f: friends) {
			g.setColor(BACKGROUND_COLOUR);
			g.fillRoundRect(buffer*3+(fW+buffer*2)*(i%2), 
					buffer*2+(int)(i/2)*fH, fW, fH-buffer*2, buffer*2, buffer*2);
			
			g.setColor(Consts.PALE);
			g.drawString(f.username, buffer*7+(fW+buffer*2)*(i%2), 
					buffer*2+(int)(i/2)*fH+(int)(0.6*fH));
			
			i++;
		}
	}

}
