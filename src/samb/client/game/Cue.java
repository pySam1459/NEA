package samb.client.game;

public class Cue {
	/* The Cue class contains information about the cue, used by the Table class
	 * */
	
	public boolean show = false;
	public boolean set = false;
	
	public double angle = 0.0;
	public double power = 0.0;
	public double startDist = 0.0;
	
	public Cue() {
		reset();
		
	}
	
	public void reset() {
		show = false;
		set = false;
		
		angle = 0.0;
		power = 0.0;
		startDist = 0.0;
		
	}
	
	public void halfReset() {
		power = 0.0;
		startDist = 0.0;
		set = false;
		
	}

}
