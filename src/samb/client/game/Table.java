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
import samb.client.utils.Line;
import samb.client.utils.Maths;
import samb.client.utils.datatypes.Dimf;
import samb.client.utils.datatypes.Pointf;
import samb.com.server.info.GameInfo;
import samb.com.server.info.UpdateInfo;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;
import samb.com.utils.Circle;
import samb.com.utils.enums.TableUseCase;

public class Table extends Widget {
	/* This subclass handles the table object, updating, ticking and rendering
	 * I have programmed this table so that a Table object can be used players, spectators or users practising.
	 * I decided to render the balls on a large image (2048 x 1024) and re-scale it to the shape of the table
	 *   being rendered on the window
	 * This can cause some complexity with scaling issues, but it reduces the complexity of ball co-ordates and different sized windows
	 * */
	
	public static final Dimension tdim = new Dimension(2048, 1024);
	private static final Dimension imgDim = new Dimension(1566, 860);
	private static final Dimension imgBDim = new Dimension(1408, 704);
	private static Dimf bdim;
	private static Pointf bxy;
	private static final Line[] cushions = getCushions();
	
	public TableUseCase tuc;
	public boolean turn = false;
	private boolean allowAim = true;
	
	private Cue cue;
	private Ball cueBall;
	
	private List<Ball> balls;
	private UpdateInfo updateInfo;
	
	private Pocket[] pockets;
	
	private Client client;
	private GamePage gp;

	public Table(Client client, GamePage gp) {
		super(calculateRect(3*Window.dim.width/4));
		this.client = client;
		this.gp = gp;
		
		this.cue = new Cue();
		this.balls = new ArrayList<>();
		createPockets();
		
	}
	
	private static int[] calculateRect(int maxWidth) {
		// Returns the rectangle which the table will take up on the screen
		final int buffer = 48;
		// {gw, gh} is the width and height of the rendered table
		final int gw = maxWidth - buffer*2;
		final int gh = gw * imgDim.height / imgDim.width;
		
		// bdim and bxy is the boundary dimensions and top left coords (boundary for the balls, ie the cushions)
		bdim = new Dimf(imgBDim.width * (double)gw/imgDim.width, 
						imgBDim.height * (double)gh/imgDim.height);
		
		bxy = new Pointf((gw - bdim.width) / 2, (gh - bdim.height) / 2);
		
		return new int[] {buffer, Window.dim.height/2 - gh/2, gw, gh};
	}
	
	public void pocket(Ball b) {
		
	}
	
	public void setUseCase(GameInfo gi, String id) {
		// Determines the use of the table (playing, spectating, practicing)
		tuc = gi.tuc;
		if(gi.tuc == TableUseCase.practicing) {
			turn = true;
			
		} else if(gi.tuc == TableUseCase.playing) {
			turn = gi.turn.equals(id);
			
		} else if(gi.tuc == TableUseCase.spectating) {
			turn = false;
		}
	}

	@Override
	public void tick() {
		tickUpdate();
		aim();
		checkNewAim();
		
		// Balls tick and update separately as collision equations use un-updated values
		for(Ball b: balls) {
			b.tick();
		} for(Ball b: balls) {
			b.update();
		} for(Pocket p: pockets) {
			p.tick();
		}
	}
	
	private void tickUpdate() {
		// If an update Packet has been sent by the host, the update will occur here
		if(updateInfo != null) {
			this.turn = client.udata.id.equals(updateInfo.turn);
			
			cueBall.vx = updateInfo.vx;
			cueBall.vy = updateInfo.vy;
			
			updateInfo = null;

		}
	}
	
	private void aim() {
		// TODO might consider showing cue to spectators/other player
		// This method controls how the user aims the cue, 
		//   first getting an angle "set", then changing the "power" and finally shooting
		
		if(tuc != TableUseCase.spectating) {
			cue.show = (tuc == TableUseCase.practicing || turn) && allowAim;
			
			if(cue.show) {
				if(!cue.set) {
					Pointf xy = getMouseOnTable();
					cue.angle = Maths.getAngle(new Pointf(cueBall.x, cueBall.y), xy);
	
					// If the user wants this angle
					if(Client.mouse.left && Client.mouse.forleft < 2) {
						cue.set = true;
						cue.startDist = Maths.getDis(xy.x, xy.y, cueBall.x, cueBall.y);
					}
					
				} else if(Client.mouse.left) {
					Pointf xy = getMouseOnTable();
					//double angle = Maths.getAngle(new Pointf(cueBall.x, cueBall.y), xy);
					double distance = Maths.getDis(xy.x, xy.y, cueBall.x, cueBall.y);
					
					cue.power = cue.startDist - distance;
					
				} else if(!Client.mouse.left && cue.power > 2.5) {
					shoot();
					
				} else {
					cue.halfReset();

				}
			}
		}
	}
	
	private Pointf getMouseOnTable() {
		// Returns the mouse's XY on the table
		Point p = Client.mouse.getXY();
		return toTable(new Pointf(p.x-rect[0], p.y-rect[1]));
		
	}
	
	private void shoot() {
		// This methods sends an update packet to the host about the new velocity of the cue ball
		
		double[] vel = Maths.getVelocity(cue.angle, cue.power);
		turn = tuc != TableUseCase.playing;
		
		if(tuc == TableUseCase.playing) {
			Packet p = createUpdate(vel);
			client.server.send(p);
			
		} else if(tuc == TableUseCase.practicing) {
			cueBall.vx = vel[0];
			cueBall.vy = vel[1];
			
		}
		
		allowAim = false;
		cue.reset();
		
	}
	
	private void checkNewAim() {
		// This method checks whether the player is allowed to aim or whether to wait
		//   ie when the balls are still moving = invalid
		
		if(turn && tuc != TableUseCase.spectating) {
			boolean newAim = true;
			for(Ball b: balls) {
				if(b.moving) {
					newAim = false;
					
				}
			} 
			allowAim = newAim;
			
		}
	}
	
	public void rack(GameInfo gi) {
		// To "rack" is to set up the table, therefore all the relevant ball info is transfered to the table
		
		this.balls = new ArrayList<>();
		Ball b;
		for(Circle c: gi.balls) {
			b = new Ball(c, this.balls);
			balls.add(b);
			
			if(c.col == 0) { // If the ball is the cue ball
				cueBall = b;
			}
		}
	}
	
	
	// Synchronisation methods
	public void update(UpdateInfo upinfo) {
		this.updateInfo = upinfo;
		
	}
	
	public Packet createUpdate(double[] vel) {
		// This method creates an update Packet, sends the velocity of the cue ball
		
		Packet p = new Packet(Header.updateGame);
		p.updateInfo = new UpdateInfo(turn ? gp.info.id : gp.info.opp);
		p.updateInfo.vx = vel[0];
		p.updateInfo.vy = vel[1];
		
		return p;
	}
	
	public List<Circle> getCircles() {
		// This method converts the balls to Circle objects (less data to send)
		
		List<Circle> circles = new ArrayList<>();
		for(Ball b: balls) {
			circles.add(b);
		}
		
		return circles;
	}
	
	public List<Ball> getBalls() {
		return balls;
	}
	
	
	// Render Methods
	@Override
	public void render(Graphics2D graph) {
		// The render method is called by the GamePage object and must render
		//   the table image -> the balls -> the cue -> any WidgetAnimations
		
		BufferedImage img = new BufferedImage(rect[2], rect[3], BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		g.drawImage(ImageLoader.get("table.png"), 0, 0, rect[2], rect[3], null);
		renderBalls(g);
		
		graph.drawImage(img, rect[0], rect[1], rect[2], rect[3], null);
		super.animRender(graph); // Renders any animations
		
		renderCue(graph);
	} 
	
	private void renderBalls(Graphics2D g) {
		final int buffer = 64; // the buffer allows the balls going into pockets to be rendered
		
		// The ballsImg is 2048x1024, so it needs to be scaled down to bdim
		BufferedImage ballsImg = getBallsImage(buffer);
		g.drawImage(ballsImg, (int)bxy.x-buffer, (int)bxy.y-buffer, (int)bdim.width+buffer*2, (int)bdim.height+buffer*2, null);
		
	}
	
	private BufferedImage getBallsImage(int buffer) {
		// This method returns an image with the balls scaled to their position on the table
		final int scaledBuffer = (int) (buffer * (tdim.width / bdim.width));
		
		BufferedImage img = new BufferedImage(tdim.width + scaledBuffer*2, tdim.height + scaledBuffer*2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		g.setColor(Color.ORANGE);
		for(Line l: cushions) {
			g.drawLine((int)l.x1-buffer*2, (int)l.y1-buffer*2, 
					(int)l.x2-buffer*2, (int)l.y2-buffer*2);
		}
		
		for(Pocket p: pockets) {
			p.render(g, scaledBuffer);
		}
		
		for(Ball b: balls) {
			b.render(g, scaledBuffer);
		}
		
		return img;
		
	}
	
	private void renderCue(Graphics2D g) {
		// Currently, the cue is rendered with 2 colours lines, instead of an image (maybe change later)
		
		if(cue.show) {
			// Some constant to modifiy the length, thickness, offset of the cue
			final double projectionLength = 384;
			final double cueLength = 256;
			final double offset = Ball.DEFAULT_BALL_RADIUS*1.5;
			final int thickness = 4;
			
			Pointf cueft = fromTable(new Pointf(cueBall.x, cueBall.y));
			cueft.x += rect[0];
			cueft.y += rect[1];
			
			// Line 1
			double[] start = Maths.getProjection(cue.angle, cue.power/2 + offset, cueft);
			double[] end = Maths.getProjection(cue.angle, cue.power/2 + projectionLength + offset, cueft);
			
			g.setStroke(new BasicStroke(thickness));
			g.setColor(Color.GRAY);
			g.drawLine((int)start[0], (int)start[1], (int)end[0], (int)end[1]);
			
			// Line 2
			start = Maths.getProjection(cue.angle, cue.power/2 + offset, cueft);
			end = Maths.getProjection(cue.angle, cue.power/2 + cueLength + offset, cueft);
			
			g.setColor(Color.YELLOW);
			g.drawLine((int)start[0], (int)start[1], (int)end[0], (int)end[1]);
			
		}
	}
	
	
	// Some methods to map points to and from the table dimensions
	public Pointf toTable(Pointf p) {
		return new Pointf((p.x - bxy.x) * (tdim.width / bdim.width),
						(p.y - bxy.y) * (tdim.height / bdim.height));
	}

	public Pointf fromTable(Pointf p) {
		return new Pointf(p.x * (bdim.width / tdim.width) + bxy.x,
						p.y * (bdim.height / tdim.height) + bxy.y);
	}
	
	
	// These methods are at the bottom as they take up space and look ugly
	private void createPockets() {
		int r = 96, off=38;
		this.pockets = new Pocket[] {
			new Pocket(-off, -off, r, this),
			new Pocket(tdim.width/2, -60, 64, this),
			new Pocket(tdim.width+off, -off, r, this),
			new Pocket(-off, tdim.height+off, r, this),
			new Pocket(tdim.width/2, tdim.height+60, 64, this),
			new Pocket(tdim.width+off, tdim.height+off, r, this)
		};
	}
	
	private static Line[] getCushions() {
		// These are the coordinates for each cushion, including pocket cushions
		return new Line[] {
			new Line(108, 242, 160, 290),
			new Line(160, 290, 1046, 290),
			new Line(1046, 290, 1060, 248),
			new Line(1151, 248, 1163, 290),
			new Line(1163, 290, 2047, 290),
			new Line(2047, 290, 2104, 242),
			new Line(120, 1356, 160, 1314),
			new Line(160, 1314, 1046, 1314),
			new Line(1046, 1314, 1056, 1350),
			new Line(1154, 1346, 1163, 1314),
			new Line(1163, 1314, 2047, 1314),
			new Line(2047, 1314, 2085, 1352),
			new Line(29, 317, 80, 370),
			new Line(80, 370, 80, 1230),
			new Line(80, 1230, 36, 1278),
			new Line(2175, 321, 2130, 370),
			new Line(2130, 370, 2130, 1230),
			new Line(2130, 1230, 2168, 1268),
		};
	}

}
