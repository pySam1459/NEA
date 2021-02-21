package samb.client.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import samb.client.utils.Maths;
import samb.com.utils.Circle;

public class Ball extends Circle {
	/* An object of this subclass represents a single ball on the pool table
	 * This class handles: movement, collision detection + action, rendering for a single ball
	 * NOTE: The 'Func' class handles most of the collision mathematics 
	 * */
	
	private static final long serialVersionUID = -6433658309710972703L;
	public static final Color[] colours = new Color[] {new Color(231, 223, 193), new Color(254, 63, 32), new Color(255, 170, 0), new Color(17, 18, 20)};
	//public static final double TABLE_FRICTION = 1, BALL_FRICTION = 1;
	public static final double TABLE_FRICTION = 0.973, BALL_FRICTION = 0.85, SPEED_THRESHOLD = 1;
	
	public List<Ball> collidedWith = new ArrayList<>();
	public boolean moving = false;
	
	private List<Ball> all;
	
	public Ball(Circle c, List<Ball> all) {
		super(c.x, c.y, c.vx, c.vy, c.r, c.col);
		
		this.all = all;
		
	}
	
	public void tick() {
		move();
		collisionBounds();
		collisionBalls();
		
	}
	
	public void move() {
		this.x += this.vx;
		this.y += this.vy;
		
	}
	
	private void collisionBounds() {
		// This method checks the bounding collision (ie the cushions)
		if(x-r <= 0) {
			this.x = r;
			this.vx *= -1;
		} if(y-r <= 0) {
			this.y = r;
			this.vy *= -1;
		}
		
		if(x+r > Table.tdim.width) {
			this.x = Table.tdim.width-r;
			this.vx *= -1;
		} if(y+r > Table.tdim.height) {
			this.y = Table.tdim.height-r;
			this.vy *= -1;
		}
	}
	
	private void collisionBalls() {
		// This method checks if the ball has collided with other balls on the table
		// If it has, then it will do the relevant calculations to produce the mathematically accurate reaction
		for(Ball b: all) {
			if(b != this) {
				Maths.collision(this, b);
				
			}
		}
	}
	
	public void update() {
		// This method applies friction to the velocity and checks if the balls is still moving, or not
		
		this.vx *= TABLE_FRICTION;
		this.vy *= TABLE_FRICTION;
		
		if(Maths.magnitude(vx, vy) > Ball.SPEED_THRESHOLD) {
			this.moving = true;
			
		} else {
			vx = 0.0;
			vy = 0.0;
			moving = false;
		}

		this.collidedWith.clear();
	}
	
	public void render(Graphics2D g, int off) {		
		g.setColor(colours[col]);
		g.fillOval((int)(x-r)+off, (int)(y-r)+off, (int)(r*2), (int)(r*2));

	}

}
