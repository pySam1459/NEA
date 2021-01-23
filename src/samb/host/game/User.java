package samb.host.game;

import java.net.DatagramPacket;
import java.net.InetAddress;

import samb.com.server.packet.Packet;
import samb.com.server.packet.PacketFactory;
import samb.host.main.Host;

public class User {

	private InetAddress addr;
	private int port;

	public String id, username;
	
	public boolean waiting;
	
	private Host h;

	public User(Host h, Packet p, DatagramPacket packet) {
		this.addr = packet.getAddress();
		this.port = packet.getPort();
		
		this.id = p.id;
		this.username = p.loginInfo.username;
		
		this.h = h;
	}
	
	public void send(Packet p) {
		byte[] data = PacketFactory.getBytes(p);
		h.server.send(new DatagramPacket(data, data.length, addr, port));
		
	}
	
}
