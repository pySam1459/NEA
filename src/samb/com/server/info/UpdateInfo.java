package samb.com.server.info;

public class UpdateInfo {
	
	public double vx, vy;
	public String turn;
	
	public UpdateInfo(String turn) {
		this.turn = turn;
		
	}
	
	public UpdateInfo(double vx, double vy) {
		this.vx = vx;
		this.vy = vy;
		
	}

}
