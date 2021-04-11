package samb.client.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import samb.client.main.Client;
import samb.client.utils.Consts;
import samb.client.utils.Maths;
import samb.com.utils.Circle;
import samb.com.utils.data.Line;

public class Ball extends Circle {
	/* An object of this subclass represents a single ball on the pool table
	 * This class handles: movement, collision detection + action, rendering for a single ball
	 * NOTE: The 'Maths' class handles most of the collision mathematics 
	 * */
	
	private static final long serialVersionUID = -6433658309710972703L;
	public static final Color[] colours = new Color[] {new Color(231, 223, 193), new Color(254, 63, 32), new Color(255, 170, 0), new Color(17, 18, 20)};
	//public static final double TABLE_FRICTION = 1, BALL_FRICTION = 1;
	private double[] NON_CUSHION_RECT; // TABLE_FRICTION = 0.0035
	
	public List<Ball> collidedWith = new ArrayList<>();
	public boolean moving = false;
	
	private List<Ball> all;
	
	public Ball(Circle c, List<Ball> all) {
		super(c.x, c.y, c.vx, c.vy, c.r, c.col);
		
		this.NON_CUSHION_RECT = new double[] {r*2, r*2, Table.tdim.width-r*4, Table.tdim.height-r*4};
		this.all = all;
		
	}
	
	public void tick() {
		move();
		collisionCushions();
		collisionBalls();
		
	}
	
	public void move() {
		// This method adds the velocity onto the x,y position of the ball
		this.x += this.vx*Client.dt/Consts.FINE_TUNE_ITERS;
		this.y += this.vy*Client.dt/Consts.FINE_TUNE_ITERS;
		
	}
	
	
	private void collisionCushions() {
		// This method checks whether the ball has collided with a cushion
		// If it has, then the ball will bounce off
		
		if(!Maths.ballInRect(this, NON_CUSHION_RECT) || true) {
			for(Line l: Table.cushions) {
				if(Maths.lineInBall(this, l)) {
					if(l.x1 == l.x2) {
						this.vx *= -Consts.CUSHION_FRICTION;
						this.x = this.x > l.x1 ? l.x1+r+1 : l.x1-r-1;
						
					} else if(l.y1 == l.y2) {
						this.vy *= -Consts.CUSHION_FRICTION;
						this.y = this.y > l.y1 ? l.y1+r+1 : l.y1-r-1;
						
					} else {
						Maths.ballCollisionLine(this, l);
						
					}
				}
			}
		}
	}
	
	private void collisionBalls() {
		// This method checks if the ball has collided with other balls on the table
		// If it has, then it will do the relevant calculations to produce the mathematically accurate reaction
		for(Ball b: all) {
			if(b != this) {
				if(Maths.ballCollisionBall(this, b) && this.col == 0) {
					Table.getTable().checkCollisionFoul(b);
					
				}
			}
		}
	}
	
	public void update() {
		// This method applies friction to the velocity and checks if the balls is still moving, or not
		double friction = Consts.TABLE_FRICTION*Client.dt/(Consts.FINE_TUNE_ITERS*Maths.magnitude(vx, vy));

		if(friction < 1) {
			this.vx *= 1-friction;
			this.vy *= 1-friction;
		}
		
		if(Maths.magnitude(vx, vy) > Consts.SPEED_THRESHOLD) {
			// Checks whether the ball is 'moving' (moving faster enough to be seen to be moving)
			this.moving = true;
			
		} else {
			vx = 0.0;
			vy = 0.0;
			moving = false;
		}

		this.collidedWith.clear();
	}
	
	public void render(Graphics2D g, int off) {		
		// Renders the ball
		g.setColor(colours[col]);
		g.fillOval((int)(x-r)+off, (int)(y-r)+off, (int)(r*2), (int)(r*2));

	}

}
