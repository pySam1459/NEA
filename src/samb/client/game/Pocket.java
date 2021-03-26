package samb.client.game;

import java.awt.Color;
import java.awt.Graphics2D;

import samb.client.utils.Maths;
import samb.com.utils.Circle;

public class Pocket extends Circle {
	/* This class represents a pocket on the table
	 * 6 pockets will check whether a ball has 'fallen' in every game tick
	 * if so, the table class 'pocket' method will be called
	 * */

	private static final long serialVersionUID = -2691551611350119501L;
	private Table t;
	
	public Pocket(double x, double y, double r, Table t) {
		super(x, y, r, 0); // x, y, r, colour
		this.t = t;
		
	}
	
	public void tick() {
		for(Ball b: t.getBalls()) {
			if(Maths.circle2(this, b)) {
				t.pocket(b);
				
			}
		}
	}
	
	public void render(Graphics2D g, int off) {
		// Development and Debugging Purposes
		
		g.setColor(new Color(0, 0, 255, 127));
		g.fillOval((int)(x-r)+off, (int)(y-r)+off, (int)(r*2), (int)(r*2));
		
	}
}
