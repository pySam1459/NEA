package samb.client.page.widget;

import java.awt.BasicStroke;
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
	
	private int scroll = 0, scrollSpeed=4, buffer=4, highlight=-1;

	private int fh = 48;
	private List<Friend> friends;
	
	public FriendList(int[] rect) {
		super(rect);
		
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
			int index = 2*((scroll + xy.y - rect[1]) % fh);
			if(xy.x > rect[0]+rect[2]/2) {
				index++;
			}
			this.highlight = index;
			
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
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent m) {
		int maxS = (int)(friends.size()/2)*fh;
		if(maxS < rect[3]) {
			this.scroll = 0;
			return;
		}
		
		int newscroll = scroll + scrollSpeed * m.getWheelRotation();
		
		if(newscroll < 0) {
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
		renderFriends(g);
		
		graph.drawImage(img, rect[0], rect[1], null);
		super.animRender(graph);
	}
	
	private void renderBackground(Graphics2D g) {
		g.setColor(Consts.MENU_BACKGROUND_COLOR);
		g.fillRoundRect(buffer, buffer, rect[2]-buffer*2, rect[3]-buffer*2, buffer*2, buffer*2);
		
		g.setStroke(new BasicStroke(2));
		g.setColor(Consts.MENU_BORDER_COLOR);
		g.drawRoundRect(buffer, buffer, rect[2]-buffer*2, rect[3]-buffer*2, buffer*2, buffer*2);
		
	}
	
	private void renderFriends(Graphics2D g) {
		int i = 0;
		for(Friend f: friends) {
			if(i == highlight) {
				g.setColor(new Color());
			}
			
			g.setColor(Consts.PALE);
			g.drawString(f.username, );
			
			i++;
		}
	}

}
