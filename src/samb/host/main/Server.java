package samb.host.main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import samb.com.server.BaseProcessor;
import samb.com.server.BaseServer;
import samb.com.server.packet.Packet;
import samb.com.server.packet.PacketFactory;

public class Server extends BaseServer {
	/* This class is a subclass of the abstract BaseServer class
	 * A specific DatagramSocket port is used so that Players know which port to send their packets to
	 * */
	private final String SIP = "192.168.1.178";

	public Server(BaseProcessor processor) {
		super(processor);

	}

	@Override
	protected void createSocket() {
		try {
			// PORT 5303 is the default port for the host server
			this.socket = new DatagramSocket(PORT);
			
		} catch(SocketException e) {
			e.printStackTrace();
			
		}
	}
	
	public void send(DatagramPacket packet) {
		try {
			socket.send(packet);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendTo(Packet p, InetAddress addr, int port) {
		// This method can be used if a Packet has to be sent to some unknown device, ie. A device with the correct details (wrong password, etc)
		
		byte[] data = PacketFactory.getBytes(p);
		DatagramPacket packet = new DatagramPacket(data, data.length, addr, port);
		send(packet);
		
	}
	
	public void start(int port) {
		this.PORT = port;
		processor.startThread();
		super.start();
		System.out.printf("Server has started, listening on  %s:%d\n", SIP, PORT);
	}
	
	public void stop() {
		processor.stopThread();
		super.stop();
		
	}

}
