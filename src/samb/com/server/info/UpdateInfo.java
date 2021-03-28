package samb.com.server.info;

import java.io.Serializable;

public class UpdateInfo implements Serializable {
	
	private static final long serialVersionUID = -2831208102218250201L;
	public double vx, vy;
	
	public UpdateInfo() {}
	
	public UpdateInfo(double vx, double vy) {
		this.vx = vx;
		this.vy = vy;
		
	}

}
