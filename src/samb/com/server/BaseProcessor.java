package samb.com.server;

import java.net.DatagramPacket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class BaseProcessor {
	/* This abstract class handles the received DatagramPackets
	 * When the BaseServer passes a received DatagramPacket, it will be added to the Queue
	 * The Processor may receive many DatagramPackets in a short space of time, so the queue holds the DatagramPackets until they can be processed.
	 * When the process method is called, the DatagramPackets in the queue (at that instants) will be handled by the subclass
	 * Both the Client and the Host will use inherit from this class and define their own handle method
	 * */
	
	private volatile boolean processing = false;
	private Thread processThread;
	
	protected BlockingQueue<DatagramPacket> queue;
	
	public BaseProcessor() {
		this.queue = new LinkedBlockingQueue<>();
		
	}
	
	public void add(DatagramPacket packet) {
		queue.add(packet);
		
	}
	
	public void process() {
		for(int i=0; i<queue.size(); i++) {  // Polls the number of DatagramPacket at that instants, so no concurrency errors arise.
			handle(queue.poll());
			
		}
	}
	
	// This abstract method will be overridden by the subclass 
	public abstract void handle(DatagramPacket packet);

	
	public void startThread() {
		// Starts the Thread, the process Thread is defined as a lambda function
		//   since the Runnable interface causes issues between subclasses and this abstract super class
		
		processing = true;
		processThread = new Thread(() -> {
			DatagramPacket packet;
			while(processing) {
				packet = queue.poll();
				
				if(packet != null) {
					handle(packet);
					
				}
			}
		}, 
				"Processing Thread");
		
		processThread.start();
		
	}
	
	public void stopThread() {
		// Stops the Thread
		if(processThread != null) {
			processing = false;
			processThread.interrupt();
		}
	}
	
}
