package samb.com.server.info;

import java.io.Serializable;

import samb.client.utils.datatypes.Pointf;

public class UpdateInfo implements Serializable {
	/* This packet info class contains information which is used to synchronize the games
	 * The cue ball velocity after a shoot is sent, then the rest is simulated on the other user's client
	 * When a cue has been placed, the Pointf is sent, instead of all of the balls.
	 * */
	
	private static final long serialVersionUID = -2831208102218250201L;
	public double vx, vy;
	public Pointf xy;
	
	public UpdateInfo() {}
	
	public UpdateInfo(Pointf p) {
		this.xy = p;
	}
	
	public UpdateInfo(double vx, double vy) {
		this.vx = vx;
		this.vy = vy;
	}

}
