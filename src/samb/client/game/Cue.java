package samb.client.game;

import samb.com.utils.data.Pointf;

public class Cue {
	/* The Cue class contains information about the cue, used by the Table class
	 * */
	
	public boolean show = false;
	public boolean set = false;
	
	public double angle = 0.0;
	public double power = 0.0;
	public double startDist = 0.0;
	
	public Pointf start;
	
	public Cue() {
		reset();
		
	}
	
	public void reset() {
		show = false;
		set = false;
		start = null;
		
		angle = 0.0;
		power = 0.0;
		startDist = 0.0;
		
	}
	
	public void halfReset() {
		power = 0.0;
		startDist = 0.0;
		set = false;
		start = null;
		
	}

}
