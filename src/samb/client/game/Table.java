package samb.client.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import samb.client.main.Client;
import samb.client.main.Window;
import samb.client.page.widget.Widget;
import samb.client.utils.ImageLoader;
import samb.client.utils.datatypes.Dimf;
import samb.client.utils.datatypes.Pointf;
import samb.com.server.packet.GameInfo;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;
import samb.com.utils.Circle;

public class Table extends Widget {
	/* This subclass handles the table object, updating, ticking and rendering
	 * */
	
	public static final Dimension tdim = new Dimension(2048, 1024);
	private static final Dimension imgDim = new Dimension(1566, 860);
	private static final Dimension imgBDim = new Dimension(1408, 704);
	private static Dimf bdim;
	private static Pointf bxy;
	
	public boolean spectating = false;
	public boolean turn = false;
	private boolean allowAim = true;
	
	private boolean showCue = false;
	private double cueAngle = 0.0;
	private boolean cueSet = false;
	private double cuePower = 0;
	private Ball cueBall;
	
	private List<Ball> balls;
	private GameInfo updateGI;
	
	private GamePage gp;

	public Table(GamePage gp) {
		super(calculateRect(3*Window.dim.width/4));
		this.gp = gp;
		
		this.balls = new ArrayList<>();
		
	}
	
	private static int[] calculateRect(int maxWidth) {
		int buffer = 24;
		// {gw, gh} is the width and height of the rendered table
		int gw = maxWidth - buffer*2;
		int gh = gw * imgDim.height / imgDim.width;
		
		// bdim and bxy is the boundary dimensions and top left coords (boundary for the balls, ie the cushions)
		bdim = new Dimf(imgBDim.width * (double)gw/imgDim.width, 
						imgBDim.height * (double)gh/imgDim.height);
		
		bxy = new Pointf((gw - bdim.width) / 2, (gh - bdim.height) / 2);
		
		return new int[] {buffer, Window.dim.height/2 - gh/2, gw, gh};
	}

	@Override
	public void tick() {
		tickUpdate();
		aim();
		
		for(Ball b: balls) {
			b.tick();
			
		} for(Ball b: balls) {
			b.update();
			
		}
	}
	
	private void tickUpdate() {
		if(updateGI != null) {
			this.balls = new ArrayList<>();
			Ball b;
			for(Circle c: updateGI.balls) {
				b = new Ball(c, this.balls);
				balls.add(b);
				
				if(c.col == 0) {
					cueBall = b;
				}
			}
			updateGI = null;
		}
	}
	
	private void aim() {
		// TODO might consider showing cue to spectators/other player
		if(showCue = turn && !spectating && allowAim) {
			if(!cueSet) {
				Pointf xy = getMouseOnTable();
				cueAngle = getAngle(new Pointf(cueBall.x, cueBall.y), xy);

				// If the user wants this angle
				if(Client.mouse.left && Client.mouse.forleft < 2) {
					cueSet = true;
				}
				
			} else if(Client.mouse.left) {
				Pointf xy = getMouseOnTable();
				
				
				
			} else if(!Client.mouse.left && cuePower > 2.5) {
				shoot();
				
			} else {
				cuePower = 0.0;
				cueSet = false;
			}
		}
	}
	
	private Pointf getMouseOnTable() {
		// Returns the mouse's XY on the table
		Point p = Client.mouse.getXY();
		return toTable(new Pointf(p.x, p.y));
		
	}
	
	private double getAngle(Pointf c, Pointf p) {
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
	
	private void shoot() {
		allowAim = false;
		turn = false;
		
	}
	
	
	public void update(GameInfo gi) {
		this.updateGI = gi;
		
	}
	
	public Packet getUpdate() {
		Packet p = new Packet(Header.updateGame);
		GameInfo gi = gp.info;
		gi.balls = getCircles();
		
		return p;
	}
	
	private List<Circle> getCircles() {
		List<Circle> circles = new ArrayList<>();
		for(Ball b: balls) {
			circles.add(b);
		}
		return circles;
	}
	
	
	@Override
	public void render(Graphics2D graph) {
		BufferedImage img = new BufferedImage(rect[2], rect[3], BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		g.drawImage(ImageLoader.get("table.png"), 0, 0, rect[2], rect[3], null);
		renderBalls(g);
		
		graph.drawImage(img, rect[0], rect[1], rect[2], rect[3], null);
		super.animRender(graph);
		
		renderCue(graph);
	} 
	
	private void renderBalls(Graphics2D g) {
		int buffer = 64;
		
		// The ballsImg is 2048x1024, so it needs to be scaled down to bdim
		BufferedImage ballsImg = getBallsImage(buffer);
		g.drawImage(ballsImg, (int)bxy.x, (int)bxy.y, (int)bdim.width, (int)bdim.height, null);
		
		// Boundary, TODO remove later
		g.setColor(Color.ORANGE);
		g.drawRect((int)bxy.x, (int)bxy.y, (int)bdim.width, (int)bdim.height);
	}
	
	private BufferedImage getBallsImage(int buffer) {
		BufferedImage img = new BufferedImage(tdim.width + buffer*2, tdim.height + buffer*2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		for(Ball b: balls) {
			b.render(g, buffer);
		}
		
		return img;
		
	}
	
	private void renderCue(Graphics2D g) {
		if(showCue) {
			double projectionLength = 384;
			Pointf cueft = fromTable(new Pointf(cueBall.x, cueBall.y));
			cueft.x += rect[0];
			cueft.y += rect[1];
			
			double endx = cueft.x + projectionLength * Math.cos(cueAngle);
			double endy = cueft.y + projectionLength * Math.sin(cueAngle);
			
			g.setStroke(new BasicStroke(4));
			g.setColor(Color.GRAY);
			g.drawLine((int)cueft.x, (int)cueft.y, (int)endx, (int)endy);
			
			double cueLength = 256;
			int sx = (int) (cueft.x + cuePower*Math.cos(cueAngle));
			int sy = (int) (cueft.y + cuePower*Math.sin(cueAngle));
			
			int ex = (int) (cueft.x + (cuePower+cueLength) * Math.cos(cueAngle));
			int ey = (int) (cueft.y + (cuePower+cueLength) * Math.sin(cueAngle));
			
			g.setColor(Color.YELLOW);
			g.drawLine(sx, sy, ex, ey);
			
		}
	}
	
	public Pointf toTable(Pointf p) {
		return new Pointf((p.x-bxy.x) * ((double)tdim.width / bdim.width), 
				(p.y-bxy.y) * ((double)tdim.height / bdim.height));
	
	}
	
	public Pointf fromTable(Pointf p) {
		return new Pointf(bxy.x + p.x * (bdim.width / (double)tdim.width), 
				bxy.y + p.y * (bdim.height / (double)tdim.height));
		
	}
	
}
