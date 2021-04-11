package samb.client.game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import samb.client.main.Client;
import samb.client.main.Window;
import samb.client.page.GamePage;
import samb.client.page.widget.Widget;
import samb.client.utils.Consts;
import samb.client.utils.ImageLoader;
import samb.client.utils.Maths;
import samb.com.server.info.Foul;
import samb.com.server.info.GameInfo;
import samb.com.server.info.GameState;
import samb.com.server.info.Message;
import samb.com.server.info.UpdateInfo;
import samb.com.server.info.Win;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;
import samb.com.server.packet.UHeader;
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
	
	private static Table thisTable;
	public TableUseCase tuc;
	public boolean turn = false;
	private String turnName = "";
	private boolean simulate=true, cuePlacement=false, allowPlacement=true,
			doCheck = false, allowAim = true, potted=false;
	public boolean hasCollided = false, wrongFirstCollision = false;
	private Foul foul;
	
	private Cue cue;
	private Ball cueBall;
	private Pointf cueBallPlacement;
	
	private List<Ball> balls;
	private UpdateInfo updateInfo;
	
	private Pocket[] pockets;
	
	private GamePage gp;
	private GameState state;

	public Table(GamePage gp) {
		super(calculateRect(3*Window.dim.width/4));
		Table.thisTable = this;
		this.gp = gp;
		this.state = gp.state;
		
		this.cue = new Cue();
		this.balls = new ArrayList<>();
		createPockets();
		
	}


	@Override
	public void tick() {
		tickUpdate();
		aim();
		
		simulate();
		checkNewAim();
	}
	
	
	// Tick Methods
	private void tickUpdate() {
		// If an update Packet exists, this method will use it and update the table
		
		if(updateInfo != null) {
			switch(updateInfo.header) {
			case velocity:
				cueBall.vx = updateInfo.vx;
				cueBall.vy = updateInfo.vy;
				cueBall.moving = true;
				
				allowAim = false;
				doCheck = true;
				break;
				
			case placement: // cue has been placed
				Ball b = new Ball(new Circle(updateInfo.xy.x, updateInfo.xy.y, Circle.DEFAULT_BALL_RADIUS, 0), balls);
				balls.add(b);
				cueBall = b;
				break;
				
			case win:
				endGame(updateInfo.win, updateInfo.winner);
				break;
			
			}
			updateInfo = null;
		}
	}
	
	
	private void aim() {
		// This method controls how the user aims the cue, 
		//   first getting an angle "set", then changing the "power" and finally shooting
		
		if(tuc != TableUseCase.spectating) {
			cue.show = (tuc == TableUseCase.practicing || turn) && allowAim && !cuePlacement && simulate;

			if(cue.show) {
				if(!cue.set) {
					Pointf xy = getMouseOnTable();
					cue.angle = Maths.getAngle(new Pointf(cueBall.x, cueBall.y), xy);
					cue.start = xy;
	
					// User sets cue angle
					if(Client.getMouse().left && Client.getMouse().forleft < 2) { // if single left click
						cue.set = true;
						cue.startDist = Maths.getDis(xy.x, xy.y, cueBall.x, cueBall.y);
					}
					
				} else if(Client.getMouse().left) { // adjusting power
					Pointf xy = getMouseOnTable();
					cue.power = Maths.getDis(cue.start.x, cue.start.y, xy.x, xy.y);
					
				} else if(!Client.getMouse().left && cue.power > 5) { // user let go of mouse left
					shoot();
					
				} else { // half reset cue, keep showing
					cue.halfReset();
				}
				
			} else if(cuePlacement) {
				// If a foul{potCue, wrongHit} has occurred, the opposition is allowed to placed the cue on the table
				cueBallPlacement = getMouseOnTable();
				allowPlacement = checkAllowPlacement();
				
				if(Client.getMouse().left && Client.getMouse().forleft < 2 && allowPlacement) { // if user has placed
					Packet p = new Packet(Header.updateGame);
					p.updateInfo = new UpdateInfo(UHeader.placement, cueBallPlacement);
					sendUpdate(p);
					
					cuePlacement = false;
				}
			}
		}
	}
	
	private void shoot() {
		// This methods sends an update packet to the host about the new velocity of the cue ball
		
		double[] vel = Maths.getVelocity(cue.angle, cue.power);
		Packet p = createUpdate(vel);
		sendUpdate(p);

		cue.reset();
	}
	
	
	private void simulate() {
		// Balls tick and update separately as collision equations use un-updated values
		// The for FINE_TUNE loop is used to reduce the distance travelled by the balls per 'move method'
		//   so that the collisions are more realistic and that balls don't 'teleport' past a boundary or another ball
		for(int i=0; i<Consts.FINE_TUNE_ITERS; i++) {
			for(Ball b: balls) {
				b.tick();
				
			} for(Ball b: balls) {
				b.update();
				
			} 
		}
		for(Pocket p: pockets) {
			p.tick();
			
		}
	}
	
	private void checkNewAim() {
		// This method checks whether the player is allowed to aim or whether to wait
		//   ie when the balls are still moving = wait
		
		if(tuc != TableUseCase.spectating && doCheck) {
			boolean newAim = true;
			for(Ball b: balls) {
				if(b.moving) {
					newAim = false;
					
				}
			} 
			
			allowAim = newAim;
			if(allowAim && simulate) { // Turn ends
				endTurn();
				doCheck = false;
			}
		}
	}
	
	private void endTurn() {
		// This method is called at the end of a player's turn
		// It will handle any fouls/losses and turn switches
		if(foul == null) {
			if(!hasCollided) { // If the cue ball didn't collide, a foul has occurred
				warnMessage(String.format("FOUL: No ball was struck by %s", turnName));
				foul(Foul.noHit);
				
			} else if(wrongFirstCollision) { // If the cue ball collided with the wrong colour ball
				warnMessage(String.format("FOUL: %s struck the wrong colour ball", turnName));
				foul(Foul.wrongHit);
			}
		}
		
		if(foul != null) { // if a foul has occured
			dealWithFoul(this.foul, turn);
		}
		
		if((!potted || foul != null) && tuc != TableUseCase.practicing) {
			// Swap turns
			this.turn = !turn;
			this.turnName = gp.getTurnName();
			
			if(state.turnCol != 0) {
				state.turnCol = state.turnCol == 1 ? 2 : 1;
			}
		}

		// reset foul flags
		hasCollided = false;
		wrongFirstCollision = false;
		potted = false;
		this.foul = null;
		
	}
	
	public void endGame(Win win, String winner) {
		// Called when a player has won
		simulate = false;
		gp.endGame(win, winner);
		
	}
	
	private boolean checkAllowPlacement() {
		// Checks whether the cue ball is allowed to be placed on point p
		Pointf p = cueBallPlacement;
		double r = Ball.DEFAULT_BALL_RADIUS+1;
		Circle c = new Circle(p.x, p.y, r, 0);
		
		if(p.x < r || p.x > tdim.width-r || p.y < r || p.y > tdim.height-r) { // on table
			return false;
		}
		for(Ball b: balls) { // not overlapping another ball
			if(Maths.circle2(b, c)) {
				return false;
				
			}
		}
		return true;
	}
	
	
	// Fouls
	public void checkCollisionFoul(Ball b) {
		// This method is called by a ball when it has collided with another ball
		
		if(!hasCollided) {
			if(b.col == 3) { // hit black ball first
				if(getTurnScore() != 7) {
					wrongFirstCollision = true;
				}
			} else if(state.turnCol != b.col && state.turnCol != 0){
				wrongFirstCollision = true;
			}
		}
		
		hasCollided = true;
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
			
		default:
			break;
		}
	}
	
	
	public void pocket(Ball b) {
		// This method is called when a ball is pocketed
		balls.remove(b);
		
		if(b.col == 0) { // Cue Ball
			warnMessage(String.format("FOUL: %s potted the Cue ball", turnName));
			foul(Foul.potCue);
			
		} else if (b.col == 3) { // 8 Ball
			if(getTurnScore() >= 7) {
				if(state.turnCol == 1) { gp.state.redBlack = true; }
				else if(state.turnCol == 2) { gp.state.yellowBlack=true; }
				
				warnMessage(String.format("WIN: %s potted the 8 ball", turnName));
				win(gp.getTurnID(), Win.pottedAll); // turn player wins
				
			} else {
				warnMessage(String.format("LOSS: %s potted the 8 ball", turnName));
				win(gp.getNotTurnID(), Win.pottedBlack); // not turn player wins
			}
			
		} else {
			if(b.col == 1) { // Red Ball
				gp.state.red++; // increase score
				potted = true;
				warnMessage(String.format("%s potted a red", turnName));
				
				if(gp.state.redID == null && tuc != TableUseCase.practicing) {
					gp.state.redID = gp.getTurnID(); // Sets who's got what colour
					gp.state.yellowID = gp.getNotTurnID();
					gp.setMenuTitleColours();
					state.turnCol = 1; // what colour is the player who's turn it is, is
					
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
					state.turnCol = 2;
	
					String msg = String.format("Therefore %s's colour is yellow and %s's colour is red", turnName, gp.getNotTurnName());
					gp.addChat(new Message(msg, "$BOLD NOSPACE$"));
				}
			} if(state.turnCol != b.col && state.turnCol != 0 && tuc != TableUseCase.practicing) {
				foul(Foul.potWrong);
				warnMessage(String.format("FOUL: %s potted the wrong colour", turnName));
			}
		}
	}
	
	public void win(String wid, Win win) {
		// This method is called when a win is 'detected', an update is sent to the host
		this.state.win = win;
		if((turn || win == Win.forfeit) && tuc == TableUseCase.playing) {
			Packet p = new Packet(Header.updateGame);
			p.updateInfo = new UpdateInfo(UHeader.win, win, wid);
			p.gameState = state;
			Client.getClient().server.send(p);
			
		} else if(tuc == TableUseCase.practicing) {
			updateInfo = new UpdateInfo(UHeader.win, win, wid);
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
	
	 
	// Update Methods
	public void update(UpdateInfo upinfo) {
		this.updateInfo = upinfo;
		
	}
	
	public Packet createUpdate(double[] vel) {
		// This method creates an update Packet, sends the velocity of the cue ball
		Packet p = new Packet(Header.updateGame);
		p.updateInfo = new UpdateInfo(UHeader.velocity, vel[0], vel[1]);
		
		return p;
	}
	
	private void sendUpdate(Packet p) {
		// This method sends an update packet to the host (or back to itself if practising)
		if(tuc == TableUseCase.playing) {
			Client.getClient().server.send(p);
			
		} else if(tuc == TableUseCase.practicing) {
			updateInfo = p.updateInfo;
		}
	}
	
	
	private void warnMessage(String msg) {
		if(simulate) { // sends a bold message to the chat
			gp.addChat(new Message(msg, "$BOLD$"));
		
		}
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
		Point p = Client.getMouse().getXY();
		return toTable(new Pointf(p.x-rect[0], p.y-rect[1]));
		
	}
	
	private int getTurnScore() { // returns the current player's score
		return state.turnCol == 1 ? gp.state.red : gp.state.yellow;
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

		// Renders balls
		for(Ball b: balls) {
			b.render(g, scaledBuffer);
		}
		
		// Renders the cue "in hand"
		if(cuePlacement && cueBallPlacement != null && simulate) {
			g.setColor(Ball.colours[0]);
			g.fillOval((int)(cueBallPlacement.x-Circle.DEFAULT_BALL_RADIUS+scaledBuffer), 
					(int)(cueBallPlacement.y-Circle.DEFAULT_BALL_RADIUS+scaledBuffer), 
					(int)Circle.DEFAULT_BALL_RADIUS*2, (int)Circle.DEFAULT_BALL_RADIUS*2);
			
			if(!allowPlacement) { // if the cue is in an invalid spot, display with red tint
				g.setColor(new Color(255, 0, 0, 127));
				g.fillOval((int)(cueBallPlacement.x-Circle.DEFAULT_BALL_RADIUS+scaledBuffer), 
						(int)(cueBallPlacement.y-Circle.DEFAULT_BALL_RADIUS+scaledBuffer), 
						(int)Circle.DEFAULT_BALL_RADIUS*2, (int)Circle.DEFAULT_BALL_RADIUS*2);
			}
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
			
			Pointf cueft = fromTable(new Pointf(cueBall.x, cueBall.y));
			cueft.x += rect[0];
			cueft.y += rect[1];
			
			// Line 1 Handle
			double[] start = Maths.getProjection(cue.angle, cue.power/2 + offset, cueft);
			double[] end = Maths.getProjection(cue.angle, cue.power/2 + projectionLength + offset, cueft);
			
			g.setStroke(Consts.cueStroke);
			g.setColor(Color.GRAY);
			g.drawLine((int)start[0], (int)start[1], (int)end[0], (int)end[1]);
			
			// Line 2 Cue 'barrel'
			start = Maths.getProjection(cue.angle, cue.power/2 + offset, cueft);
			end = Maths.getProjection(cue.angle, cue.power/2 + cueLength + offset, cueft);
			
			g.setColor(Color.YELLOW);
			g.drawLine((int)start[0], (int)start[1], (int)end[0], (int)end[1]);
			
			
			// Shot Line Projection
			g.setStroke(Consts.cueProjectionStroke);
			final double angle = cue.angle + Math.PI;
			start = Maths.getProjection(angle, offset, cueft);
			end = Maths.getProjection(angle, offset + 1500, cueft);
			
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
	
	public void setState(GameState state) {
		// Sets the state of the table (used by spectators)
		gp.state = state;
		this.state = state;
		
		if(state.turnCol != 0) {
			gp.setMenuTitleColours();
		}
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
			new Line(40, -39, 80, 0),
			new Line(80, 0, 964, 0),
			new Line(964, 0, 973, -29),
			new Line(1075, -28, 1082, 0),
			new Line(1082, 0, 1966, 0),
			new Line(1966, 0, 2002, -35),
			
			new Line(2088, 41, 2048, 85),
			new Line(2048, 85, 2048, 941),
			new Line(2048, 941, 2078, 970),
			
			new Line(45, 1058, 82, 1023),
			new Line(82, 1023, 967, 1023),
			new Line(967, 1023, 975, 1051),
			new Line(1075, 1058, 1082, 1025),
			new Line(1082, 1025, 1970, 1025),
			new Line(1970, 1025, 2000, 1054),
			
			new Line(-25, 54, 0, 81),
			new Line(0, 81, 0, 938),
			new Line(0, 938, -25, 966)
		};
	}
	
	// Object Getter
	public static Table getTable() {
		return Table.thisTable;
	}

}
