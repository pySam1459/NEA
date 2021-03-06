package samb.client.game;

import samb.client.utils.Maths;
import samb.com.utils.Circle;
import samb.com.utils.data.Pointf;

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
		for(Ball b: t.getBalls()) { // iterates through all balls on table
			if(Maths.pointInCircle(new Pointf(b.x, b.y), this)) {
				t.pocket(b);
				
			}
		}
	}

}
