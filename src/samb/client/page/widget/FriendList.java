package samb.client.page.widget;

import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import samb.client.main.Client;
import samb.com.database.Friend;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;

public class FriendList extends Widget implements MouseWheelListener {
	
	private List<Friend> friends;

	public FriendList(int[] rect) {
		super(rect);
		
		getFriends();
	}
	
	@Override
	public void tick() {
		

	}

	@Override
	public void render(Graphics2D g) {
		

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
		

	}



}
