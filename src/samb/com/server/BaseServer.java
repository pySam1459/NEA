package samb.com.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public abstract class BaseServer implements Runnable {
	/* This abstract class handles events regarding the receiving of incoming DatagramPackets from the Internet.
	 * The subclasses of BaseServer will handle the sending of DatagramPackets (and the instantiation of the DatagramSocket)
	 * This class is designed to be used by both Client and Host servers (to reduce code complexity)
	 * This class is an example of polymorphism, composition, association and overriding.
	 * 
	 * This Class creates a seperate 'Listen' Thread to listen for incoming DatagramPackets which have been sent to the DatagramSocket
	 * Once a DatagramPacket has been received, it is then handled by the Processor object (passed into the constructor as the BaseProcessor argument)
	 * */
	
	protected DatagramSocket socket;
	protected InetAddress IP, HOST_IP;
	protected int PORT, HOST_PORT;
	
	private final int BUFFER_LENGTH = 4096;
	
	private Thread listenThread;
	private volatile boolean listening = false;
	
	protected BaseProcessor processor;
	
	public BaseServer(BaseProcessor processor) {
		this.processor = processor;
		
	}
	
	// This abstract method will be overriden by the subclasses when defining the DatagramSocket and the listening PORT number
	protected abstract void createSocket();
	
	@Override
	public void run() {
		// This method is called when the listenThread is started
		
		DatagramPacket recvPacket;
		byte[] buffer = new byte[BUFFER_LENGTH];
		
		while(listening) {
			recvPacket = new DatagramPacket(buffer, buffer.length, IP, PORT);
			
			try {
				socket.receive(recvPacket);     // The code will wait here until a packet is received (which is why a separate Thread is necessary)
				processor.add(recvPacket);      // The newly received DatagramPacket is handed of to the Processor object
				buffer = new byte[BUFFER_LENGTH];
				
			} catch (SocketException e) {}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void start() {
		// Starts the listenThread
		createSocket();
		
		listening = true;
		listenThread = new Thread(this, "Listening Thread");
		listenThread.start();
		
	}
	
	public synchronized void stop() {
		// Stops the listenThread (if it has been started)
		
		if(listenThread != null) {
			listening = false;
			listenThread.interrupt();
		} if(!socket.isClosed()) {
			socket.close();
		}
	}

}
