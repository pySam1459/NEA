package samb.com.server.info;

import java.io.Serializable;

import samb.com.server.packet.UHeader;
import samb.com.utils.data.Pointf;

public class UpdateInfo implements Serializable {
	/* This packet info class contains information which is used to synchronize the games
	 * The cue ball velocity after a shoot is sent, then the rest is simulated on the other user's client
	 * When a cue has been placed, the Pointf is sent, instead of all of the balls.
	 * */
	
	private static final long serialVersionUID = -2831208102218250201L;
	public UHeader header;
	public Pointf xy;
	public double vx, vy;
	public Win win;
	public String winner;
	
	public UpdateInfo(UHeader header) {
		this.header = header;
	}
	
	public UpdateInfo(UHeader header, Pointf p) {
		this.header = header;
		this.xy = p;
	}
	
	public UpdateInfo(UHeader header, double vx, double vy) {
		this.header = header;
		this.vx = vx;
		this.vy = vy;
	}
	
	public UpdateInfo(UHeader header, Win win, String winner) {
		this.header = header;
		this.win = win;
		this.winner = winner;
	}

}
