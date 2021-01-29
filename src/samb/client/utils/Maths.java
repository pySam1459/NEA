package samb.client.utils;

import samb.client.game.Ball;
import samb.client.utils.datatypes.Pointf;

public class Maths {
	/* This class handles all mathematics which is required, especially the collision mechanics
	 * */
	
	public static void collision(Ball b1, Ball b2) {
		// This method checks if ball b1 and ball b2 have collided
		// If they have, it will calculate the new velocities of each and update them
		
		if(ball2(b1, b2) && !b1.collidedWith.contains(b2)) {
			double di = getDis(b1.x, b1.y, b2.x, b2.y);
			double nex = (b2.x - b1.x) / di;
			double ney = (b2.y - b1.y) / di;
			double p = (b1.vx * nex + b1.vy * ney - b2.vx * nex - b2.vy * ney);
			
			b1.vx = (b1.vx - p * nex) * Ball.BALL_FRICTION;
			b1.vy = (b1.vy - p * ney) * Ball.BALL_FRICTION;
			b2.vx = (b2.vx + p * nex) * Ball.BALL_FRICTION;
			b2.vy = (b2.vy + p * ney) * Ball.BALL_FRICTION;
			
			moveApart(b1, b2);
			b1.collidedWith.add(b2);
			//b2.collidedWith.add(b1);
			
		}
	}
	
	public static void moveApart(Ball b1, Ball b2) {
		// Unlike in reality, when the 2 balls collide, they pass over eachother
		// Therefore, to correct this in simulations, we must separate them so that they don't pass over eachother
		
		double mx = (b1.x + b2.x) / 2;
		double my = (b1.y + b2.y) / 2;
		double d = getDis(b1.x, b1.y, b2.x, b2.y);
		
		b1.x = mx + b1.r * (b1.x - b2.x) / d;
		b1.y = my + b1.r * (b1.y - b2.y) / d;
		b2.x = mx + b2.r * (b2.x - b1.x) / d;
		b2.y = my + b2.r * (b2.y - b1.y) / d;
		
	}
	
	public static double getAngle(Pointf c, Pointf p) {
		// Returns the angle between Pointf c to Pointf p (cue to mouseXY)
		double angle;
		if(p.x - c.x == 0) {
			angle = Math.PI/2;
		} else {
			angle = Math.atan((p.y - c.y) / (p.x - c.x));
		}
		
		if(p.x > c.x) {
			angle += Math.PI;
		}
		return angle;
	}
	
	public static double[] getVelocity(double angle, double power) {
		// Returns the velocity of the cue ball, given the cue's power and angle
		double constant = 0.1;
		return new double[] {
				-power * Math.cos(angle) * constant,
				-power * Math.sin(angle) * constant
		};
	}
	
	public static double getDis(double x1, double y1, double x2, double y2) {
		// Returns the distance between 2 points
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
		
	}
	
	public static boolean ball2(Ball b1, Ball b2) {
		// Stands for ball squared, checks if 2 balls (circles) overlap
		return getDis(b1.x, b1.y, b2.x, b2.y) <= b1.r + b2.r;
	}
	
	public static double[] getProjection(double angle, double radius, Pointf origin) {
		// calclates a projected point from an origin with a radius and angle
		return new double[] {
				origin.x + radius * Math.cos(angle),
				origin.y + radius * Math.sin(angle)
		};
	}
	
	public static double magnitude(double vx, double vy) {
		// Returns the magnitude of the velocity 'vector' (vx, vy)
		return Math.sqrt(vx*vx + vy*vy);
		
	}

}
