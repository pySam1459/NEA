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
import samb.client.utils.Consts;
import samb.client.utils.Maths;
import samb.com.database.Friend;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;

public class FriendList extends Widget implements MouseWheelListener {
	/* This widget displays a list of all your friends
	 * */
	
	private final int scrollSpeed=4, buffer=8;
	private int scroll = 0, highlight=-1;

	private int fH = 56, fW;
	private List<Friend> friends;
	
	private Font friendNameFont;
	private final BasicStroke menuStroke = new BasicStroke(3), highlightStroke = new BasicStroke(2);
	private final Color BACKGROUND_COLOUR = new Color(0, 151, 162, 178);
	private BufferedImage img, section;
	
	public FriendList(int[] rect) {
		super(rect);
		this.fW = (rect[2]-buffer*8)/2;
		
		this.friendNameFont = Consts.INTER.deriveFont(Font.PLAIN, fH/1.8f);
		getFriends();
		
	}
	
	@Override
	public void tick() {
		getHighlight();
		super.animTick();
		
	}
	
	private void getHighlight() {
		Point xy = Client.getMouse().getXY();
		if(Maths.pointInRect(xy, rect)) {
			int index = 2*((scroll + xy.y - rect[1]-buffer*3) / (fH+buffer*2));
			if(xy.x > rect[0]+rect[2]/2) {
				index++;
			} if(0 <= index && index < friends.size() && buffer*3+(int)(index/2)*(fH+buffer*2) + fH < rect[3]) {
				this.highlight = index;
			}
			
		} else {
			this.highlight = -1;
		}
	}
	
	
	private void getFriends() {
		Packet p = new Packet(Header.getFriends);
		Client.getClient().server.send(p);
	}
	
	public void setFriends(Packet p) {
		this.friends = p.friendsInfo.friends;
		for(int i=0; i<32; i++) {
			this.friends.add(new Friend("123", "Friend #" + Integer.toString(i)));
		}
		renderFriends();
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent m) {
		int maxS = (int)(friends.size()/2)*fH;
		if(maxS < rect[3]) {
			this.scroll = 0;
			return;
		}
		
		int newscroll = scroll + scrollSpeed * m.getWheelRotation();
		
		if(newscroll < 0 || maxS < rect[3]) {
			this.scroll = 0;
		} else if(newscroll+rect[3] > maxS) {
			this.scroll = maxS - rect[3];
		} else {
			this.scroll = newscroll;
		}
	}

	@Override
	public void render(Graphics2D graph) {
		BufferedImage img = new BufferedImage(rect[2], rect[3], BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		renderBackground(g);
		renderImg(g);
		
		graph.drawImage(img, rect[0], rect[1], null);
		super.animRender(graph);
	}
	
	private void renderBackground(Graphics2D g) {
		g.setColor(Consts.MENU_BACKGROUND_COLOR);
		g.fillRect(buffer, buffer, rect[2]-buffer*2, rect[3]-buffer*2);
		
		g.setStroke(menuStroke);
		g.setColor(Consts.MENU_BORDER_COLOR);
		g.drawRect(buffer, buffer, rect[2]-buffer*2, rect[3]-buffer*2);
	}

	private void renderImg(Graphics2D g) {
		if(img != null) {
			section = img.getSubimage(0, scroll, rect[2], rect[3]-buffer*3);
			g.drawImage(section, 0, 0, null);
			
			if(highlight != -1) {
				g.setColor(Consts.PALE);
				g.setStroke(highlightStroke);
				g.drawRoundRect(buffer*3+(fW+buffer*2)*(highlight%2), 
						buffer*3+(int)(highlight/2)*(fH+buffer*2), fW, fH, buffer*2, buffer*2);
			}
		}
	}
	
	private void renderFriends() {
		int h = Math.max(rect[3], (int)(friends.size())*fH);
		this.img = new BufferedImage(rect[2], h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		g.setFont(friendNameFont);
		int i = 0;
		for(Friend f: friends) {
			g.setColor(BACKGROUND_COLOUR);
			g.fillRoundRect(buffer*3+(fW+buffer*2)*(i%2), 
					buffer*3+(int)(i/2)*(fH+buffer*2), fW, fH, buffer*2, buffer*2);
			
			g.setColor(Consts.PALE);
			g.drawString(f.username, buffer*7+(fW+buffer*2)*(i%2), 
					buffer*3+(int)(i/2)*(fH+buffer*2)+(int)(0.7*fH));
			
			i++;
		}
	}

}
