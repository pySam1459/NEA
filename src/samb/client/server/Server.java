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

public class Server extends BaseServer {
	/* This class is a subclass of the abstract BaseServer class
	 * Since this is the client Server, the port number does not need to be specific, so it can be random (but not in use)
	 * */
	
	private DatagramPacket sendPacket;
	private Client client;

	public Server(Client client) {
		super(client);
		this.client = client;
		
	}

	@Override
	protected void createSocket() {
		// This method creates a random port number and its respective DatagramSocket
		// if the port is already in use, it will repeat until it finds a valid port
		
		boolean allow = false;
		while(!allow) {
			try {
				// TODO change for the future
				this.HOST_IP = InetAddress.getByName("192.168.1.178");
				this.HOST_PORT = 5303;
				
				this.PORT = new Random().nextInt(60535)+5000;  // +5000 so only ports 5000 < PORT < 65535 are chosen, since most ports below 5000 are more commonly used
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
		// Since the client only sends packets to the host, 1 method is required for any packet to be sent
		// The client id is also added so that the host knows who the packet was sent by
		
		p.id = client.udata.id;
		byte[] data = PacketFactory.getBytes(p);
		if(data.length > 4096) { // TODO remove
			System.out.println(data.length + " " + p.header.toString());
		}
		sendPacket = new DatagramPacket(data, data.length, HOST_IP, HOST_PORT);
		
		try {
			socket.send(sendPacket);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
