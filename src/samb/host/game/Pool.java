package samb.host.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import samb.host.main.Host;

public class Pool implements Runnable {
	/* This class handles the matching of players based on their elo
	 * A separate Thead is used as a concurrent loop is required to constantly check for a match
	 * A Queue is used rotate through anyone who is looking for a match, so no one is not left out
	 * A 
	 * */
	
	private volatile boolean running = false;
	private Thread poolThread;
	
	private BlockingQueue<String> queue;
	private HashMap<Integer, CopyOnWriteArrayList<String>> bands;
	public static final int ELO_BUFFER = 100;

	private GameManager gm;
	private UserManager um;
	private Random r;
	
	public Pool(GameManager gm) {
		this.gm = gm;
		this.um = Host.getHost().um;
		
		this.queue = new LinkedBlockingQueue<>();
		this.bands = new HashMap<>();
		
		this.r = new Random();
		r.setSeed(System.nanoTime());
		
		start();
	}
	
	@Override
	public void run() {
		// The main loop which constantly tries to make matches
		String id;
		while(running) {
			id = queue.poll();
			
			if(id != null) {
				if(!match(id)) {
					queue.add(id);
					
				}
			}
		}
	}
	
	private boolean match(String id) {
		// This method matches an opponent for the user with specified id
		
		if(!um.isOnline(id)) {
			return false;
		}
		
		int elo = um.get(id).elo;
		int index = elo / ELO_BUFFER;
		List<String> ids = new ArrayList<>(); // good matches
		
		int opElo;
		for(int di=-1; di<2; di++) { // check through 3 bands, -1 +0 +1
			if(bands.get(index+di) != null) {
				for(String opId: bands.get(index+di)) {
					if(!opId.equals(id)) {
						if(um.isOnline(opId)) {
							opElo = um.get(opId).elo; // get opponents elo
							
							if(elo-ELO_BUFFER < opElo && opElo < elo+ELO_BUFFER) {
								ids.add(opId); // adds to good matches list
								
							}
						} else {
							// logged off while match making
							remove(opId);
						}
					}
				}
			}
		}	
		
		if(ids.size() == 0) { // if no good matches have been found
			return false; 
		}
		
		int i = r.nextInt(ids.size()); // get a random player from good matches
		String opId = ids.get(i);
		
		if(um.isOnline(id) && um.isOnline(opId)) {
			gm.newGame(id, opId); // create new game
			
			remove(id);
			remove(opId); // remove from queue and bands
			return true;
		}
		
		return false;
	}
	
	public void add(String id) {
		// This method adds a user to the queue, and to their respective elo band (increments of 50)
		queue.add(id);
		
		int index = getIndex(id);
		
		if(bands.get(index) == null) {
			bands.put(index, new CopyOnWriteArrayList<>());
		}
		
		bands.get(index).add(id);
	}
	
	public void remove(String id) {
		// This method removes a user from the user and their band list
		queue.remove(id);
		
		if(um.isOnline(id)) {
			int index = getIndex(id);
			if(bands.get(index) != null) {
				bands.get(index).remove(id);
			
			}
		}
	}
	
	private int getIndex(String id) {
		// Returns the band index from a player's id
		return um.get(id).elo / ELO_BUFFER;
	}

	
	public synchronized void start() {
		// Starts Pool Thread
		if(!running) {
			running = true;
			poolThread = new Thread(this, "Pool Thread");
			poolThread.start();
		}
	}
	
	public synchronized void stop() {
		// Stops pool Thread, clears memory
		if(running) {
			running = false;
			poolThread.interrupt();
			
			bands.clear();
			queue.clear();
		}
	}
	
	public void showBands() {
		// Displays the users in each bands
		bands.forEach((Integer index, CopyOnWriteArrayList<String> arr) -> {
			System.out.print(index + " ");
			for(String obj: arr) {
				System.out.print(obj + ", ");
			}
			System.out.println();
		});
	}

}
