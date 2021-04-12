package samb.client.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

import samb.client.main.Client;
import samb.com.server.BaseServer;
import samb.com.server.packet.Packet;
import samb.com.server.packet.PacketFactory;
import samb.com.utils.Config;

public class Server extends BaseServer {
	/* This class is a subclass of the abstract BaseServer class
	 * Since this is the client Server, the port number does not need to be specific, 
	 *   so it can be random (but not in use, thus I chose the range 5000->65535)
	 * */
	
	private DatagramPacket sendPacket;

	public Server() {
		super(Client.getClient());
		
	}

	@Override
	protected void createSocket() {
		// This method creates a random port number and its respective DatagramSocket
		// if the port is already in use, it will repeat until it finds a valid port
		
		boolean allow = false;
		while(!allow) {
			try {
				this.HOST_IP = InetAddress.getByName((String)Config.get("HOST_IP"));
				this.HOST_PORT = 5303;
				
				// +5000 so only ports 5000 < PORT < 65535 are chosen, since most ports below 5000 are more commonly used
				this.PORT = new Random().nextInt(60535)+5000;  
				this.socket = new DatagramSocket(PORT);
				allow = true;
				
			} catch (SocketException | UnknownHostException e) {
				allow = false;
				System.out.printf("Port # %d is in use, trying another...\n", PORT);
			}
		}
	}
	
	public void send(Packet p) {
		// This method sends Packets to the Host Server
		// The client id is added so that the host knows who the packet was sent by
		
		p.id = Client.getClient().udata.id;
		byte[] data = PacketFactory.getBytes(p);
		sendPacket = new DatagramPacket(data, data.length, HOST_IP, HOST_PORT);
		
		try {
			socket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
