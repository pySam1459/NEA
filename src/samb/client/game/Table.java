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
import samb.client.utils.Consts;
import samb.client.utils.ImageLoader;
import samb.client.utils.Maths;
import samb.com.server.info.Foul;
import samb.com.server.info.GameInfo;
import samb.com.server.info.Message;
import samb.com.server.info.UpdateInfo;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;
import samb.com.utils.Circle;
import samb.com.utils.data.Dimf;
import samb.com.utils.data.Line;
import samb.com.utils.data.Pointf;
import samb.com.utils.enums.TableUseCase;

public class Table extends Widget {
	/* This subclass handles the table object, updating, ticking, rendering and most game events
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
	public static final Line[] cushions = getCushions();
	
	public TableUseCase tuc;
	public boolean turn = false;
	private String turnName = "";
	private boolean cuePlacement=false, doCheck = false, allowAim = true, potted=false;
	public static boolean collisions = false, wrongCollision = false;
	public static int turnCol = 0;
	private Foul foul;
	
	private Cue cue;
	private Ball cueBall;
	private Pointf cueBallPlacement;
	
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


	@Override
	public void tick() {
		tickUpdate();
		aim();
		checkNewAim();
		getpos();
		
		// Balls tick and update separately as collision equations use un-updated values
		for(Ball b: balls) {
			b.tick();
		} for(Ball b: balls) {
			b.update();
		} for(Pocket p: pockets) {
			p.tick();
		}
	}
	
	
	// Game Play Methods
	private void aim() {
		// TODO might consider showing cue to spectators/other player
		// This method controls how the user aims the cue, 
		//   first getting an angle "set", then changing the "power" and finally shooting
		
		if(tuc != TableUseCase.spectating) {
			cue.show = (tuc == TableUseCase.practicing || turn) && allowAim && !cuePlacement;

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
			} else if(cuePlacement) {
				cueBallPlacement = getMouseOnTable();
				
				if(Client.mouse.left && Client.mouse.forleft < 2) {
					Packet p = new Packet(Header.updateGame);
					p.updateInfo = new UpdateInfo(cueBallPlacement);
					
					if(tuc == TableUseCase.playing) {
						Client.getClient().server.send(p);
						
					} else if(tuc == TableUseCase.practicing) {
						updateInfo = p.updateInfo;
					}
					
					cuePlacement = false;
				}
			}
		}
	}
	
	private void shoot() {
		// This methods sends an update packet to the host about the new velocity of the cue ball
		
		double[] vel = Maths.getVelocity(cue.angle, cue.power);
		Packet p = createUpdate(vel);
		
		if(tuc == TableUseCase.playing) {
			client.server.send(p);
			
		} else if(tuc == TableUseCase.practicing) {
			updateInfo = p.updateInfo;
			
		}
		
		cue.reset();
		
	}
	
	private void checkNewAim() {
		// This method checks whether the player is allowed to aim or whether to wait
		//   ie when the balls are still moving = invalid
		
		if(tuc != TableUseCase.spectating && doCheck) {
			boolean newAim = true;
			for(Ball b: balls) {
				if(b.moving) {
					newAim = false;
					
				}
			} 
			
			allowAim = newAim;
			if(allowAim) { // Turn ends
				endTurn();
				doCheck = false;
			}
		}
	}
	
	private void endTurn() {
		// This method is called at the end of a player's turn
		// It will handle any fouls/losses and turn switches
		if(foul == null) {
			if(!Table.collisions) { // If the cue ball didn't collide, a foul has occurred
				warnMessage(String.format("FOUL: No ball was struck by %s", turnName));
				foul(Foul.noHit);
				
			} else if(Table.wrongCollision) { // If the cue ball collided with the wrong colour ball
				warnMessage(String.format("FOUL: %s struck the wrong colour ball", turnName));
				foul(Foul.wrongHit);
			}
		}
		
		if((!potted || foul != null) && tuc != TableUseCase.practicing) {
			// Swap turns
			this.turn = !turn;
			this.turnName = gp.getTurnName();
			
			if(Table.turnCol != 0) {
				Table.turnCol = turnCol == 1 ? 2 : 1;
			}
		}

		Table.collisions = false;
		Table.wrongCollision = false;
		potted = false;
		
		if(foul != null) {
			dealWithFoul(this.foul, !turn);
		}
	}
	
	private void warnMessage(String msg) {
		gp.addChat(new Message(msg, "$BOLD$"));
		
	}
	
	private void foul(Foul foul) {
		this.foul = foul;
		
	}
	
	private void dealWithFoul(Foul foul, boolean self) {
		// This method deals with each foul/loss after a player's turn
		
		switch(foul) {
		case potCue: // foul, cue ball is moved afterwards
		case wrongHit:
			if(!self || tuc == TableUseCase.practicing) {
				cuePlacement = true;
				
			} if(cueBall != null && balls.contains(cueBall)) {
				balls.remove(cueBall);
			}
			break;
			
		case potWrong:
		case noHit: // foul, cue ball is not moved afterwards
			break;
			
		case potBlack:  // loss
			break;
			
		}
		this.foul = null;
	}
	
	
	public void pocket(Ball b) {
		// This method is called when a ball is pocketed
		balls.remove(b);
		
		if(b.col == 0) { // Cue Ball
			warnMessage(String.format("FOUL: %s potted the Cue ball", turnName));
			foul(Foul.potCue);
			
		} else if (b.col == 3) { // 8 Ball
			if(getTurnScore() == 7) {
				warnMessage(String.format("WIN: %s potted the 8 ball", turnName));
				// win();
				
			} else {
				warnMessage(String.format("LOSS: %s potted the 8 ball", turnName));
				foul(Foul.potBlack);
			}
			
		} else if(b.col == 1) { // Red Ball
			gp.state.red++;
			potted = true;
			warnMessage(String.format("%s potted a red", turnName));
			
			if(gp.state.redID == null && tuc != TableUseCase.practicing) {
				gp.state.redID = gp.getTurnID();
				gp.state.yellowID = gp.getNotTurnID();
				gp.setMenuTitleColours();
				Table.turnCol = 1;
				
				String msg = String.format("Therefore %s's colour is red and %s's colour is yellow", turnName, gp.getNotTurnName());
				gp.addChat(new Message(msg, "$BOLD NOSPACE$"));
			}
			
		} else if(b.col == 2) { // Yellow Ball
			gp.state.yellow++;
			potted = true;
			warnMessage(String.format("%s potted a yellow", turnName));
			
			if(gp.state.yellowID == null && tuc != TableUseCase.practicing) {
				gp.state.yellowID = gp.getTurnID();
				gp.state.redID = gp.getNotTurnID();
				gp.setMenuTitleColours();
				Table.turnCol = 2;

				String msg = String.format("Therefore %s's colour is yellow and %s's colour is red", turnName, gp.getNotTurnName());
				gp.addChat(new Message(msg, "$BOLD NOSPACE$"));
			}
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
	
	private void tickUpdate() {
		// If an update Packet exists, this method will use it and update the table
		
		if(updateInfo != null) {
			if(updateInfo.xy != null) {
				Ball b = new Ball(new Circle(updateInfo.xy.x, updateInfo.xy.y, Circle.DEFAULT_BALL_RADIUS, 0), balls);
				balls.add(b);
				cueBall = b;
				
			} else {
				cueBall.vx = updateInfo.vx;
				cueBall.vy = updateInfo.vy;
				cueBall.moving = true;
				
				allowAim = false;
				doCheck = true;
			}
			
			updateInfo = null;
		} 
	}
	
	public Packet createUpdate(double[] vel) {
		// This method creates an update Packet, sends the velocity of the cue ball
		
		Packet p = new Packet(Header.updateGame);
		p.updateInfo = new UpdateInfo(vel[0], vel[1]);
		
		return p;
	}
	
	
	// Getters (sort of)
	public List<Circle> getCircles() {
		// This method converts the balls to Circle objects (less data to send)
		
		List<Circle> circles = new ArrayList<>();
		for(Ball b: balls) {
			circles.add(b);
		}
		
		return circles;
	}
	
	public Ball[] getBalls() {
		// Returns and array of Ball objects
		return balls.toArray(new Ball[0]);
	}
	
	private Pointf getMouseOnTable() {
		// Returns the mouse's XY on the table
		Point p = Client.mouse.getXY();
		return toTable(new Pointf(p.x-rect[0], p.y-rect[1]));
		
	}
	
	private int getTurnScore() {
		return Table.turnCol == 1 ? gp.state.red : gp.state.yellow;
	}
	
	private void getpos() {
		if(Consts.DEV_SHOW_MOUSE_POS && Client.mouse.left && !Client.mouse.prevLeft) {
			System.out.println(getMouseOnTable());
		
		}
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
//		
//		g.setColor(Color.ORANGE);
//		g.setStroke(new BasicStroke(2));
//		for(Line l: cushions) {
//			g.drawLine((int)l.x1+scaledBuffer, (int)l.y1+scaledBuffer, 
//					(int)l.x2+scaledBuffer, (int)l.y2+scaledBuffer);
//		}

		for(Ball b: balls) {
			b.render(g, scaledBuffer);
		}
		
		if(cuePlacement && cueBallPlacement != null) {
			g.setColor(Ball.colours[0]);
			g.fillOval((int)(cueBallPlacement.x-Circle.DEFAULT_BALL_RADIUS+scaledBuffer), 
					(int)(cueBallPlacement.y-Circle.DEFAULT_BALL_RADIUS+scaledBuffer), 
					(int)Circle.DEFAULT_BALL_RADIUS*2, (int)Circle.DEFAULT_BALL_RADIUS*2);
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
			
			
			// Shot Line
			final double angle = cue.angle + Math.PI;
			start = Maths.getProjection(angle, offset, cueft);
			end = Maths.getProjection(angle, offset + 1000, cueft);
			
			g.setColor(new Color(127, 127, 127, 127));
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
	
	
	// Initialization methods
	private static int[] calculateRect(int maxWidth) {
		// Returns the rectangle which the table will take up on the screen
		final int buffer = 48;
		// {gw, gh} is the width and height of the rendered table
		final int gw = maxWidth - buffer*2;
		final int gh = gw * imgDim.height / imgDim.width;
		
		// bdim and bxy is the boundary dimensions and top left coords (boundary for the balls, ie the cushions)
		bdim = new Dimf(imgBDim.width * (double)gw/imgDim.width, 
						imgBDim.height * (double)gh/imgDim.height);
		
		bxy = new Pointf((gw - bdim.width) / 2.0, (gh - bdim.height) / 2.0);
		
		return new int[] {buffer, Window.dim.height/2 - gh/2, gw, gh};
	}
	
	public void setUseCase(GameInfo gi, String id) {
		// Determines the use of the table (playing, spectating, practicing)
		tuc = gi.tuc;
		if(gi.tuc == TableUseCase.practicing) {
			turn = true;
			
		} else if(gi.tuc == TableUseCase.playing) {
			turn = gi.u1.id.equals(id);
			
		} else if(gi.tuc == TableUseCase.spectating) {
			turn = false;
		}
		turnName = gp.getTurnName();
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
			new Line(38, -39, 78, 0),
			new Line(78, 0, 966, 0),
			new Line(966, 0, 973, -29),
			new Line(1073, -28, 1080, 0),
			new Line(1080, 0, 1968, 0),
			new Line(1968, 0, 2003, -35),
			
			new Line(2088, 39, 2048, 83),
			new Line(2048, 83, 2048, 943),
			new Line(2048, 943, 2078, 970),
			
			new Line(45, 1058, 82, 1023),
			new Line(82, 1023, 967, 1023),
			new Line(967, 1023, 975, 1051),
			new Line(1075, 1058, 1082, 1025),
			new Line(1082, 1025, 1970, 1025),
			new Line(1970, 1025, 2000, 1054),
			
			new Line(-25, 54, 0, 81),
			new Line(0, 81, 0, 942),
			new Line(0, 942, -25, 966)
		};
	}

}
