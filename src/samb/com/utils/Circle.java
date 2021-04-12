package samb.com.utils;

import java.io.Serializable;

public class Circle implements Serializable {
	/* This class contains the bare minimum information required when sending ball information
	 * This class is used instead of the Ball class to reduce volume of data to be sent
	 * */
	
	private static final long serialVersionUID = -5151581815740461263L;
	public static final double DEFAULT_BALL_RADIUS = 24.0;
	public double x, y, vx, vy, r;
	public int col;
	
	
	public Circle() {}
	
	public Circle(double x, double y, double vx, double vy, double r, int col) {
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vy = vy;
		this.r = r;
		this.col = col;
		
	}
	
	public Circle(double x, double y, double r, int col) {
		this.x = x;
		this.y = y;
		this.vx = 0.0;
		this.vy = 0.0;
		this.r = r;
		this.col = col;
		
	}
	

}
