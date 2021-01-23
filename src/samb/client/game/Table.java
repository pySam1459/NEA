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
import samb.com.utils.Func;

public class Table extends Widget {
	/* This subclass handles the table object, updating, ticking and rendering
	 * I have programmed this table so that a Table object can be used players, spectators or users practising.
	 * I decided to render the balls on a large image (2048 x 1024) and rescale it to the shape of the table
	 *   being rendered on the window
	 * This can cause some complexity with scaling issues, but I believe that it will/has simplified other processes
	 * */
	
	public static final Dimension tdim = new Dimension(2048, 1024);
	private static final Dimension imgDim = new Dimension(1566, 860);
	private static final Dimension imgBDim = new Dimension(1408, 704);
	private static Dimf bdim;
	private static Pointf bxy;
	
	public TableUseCase tuc;
	public boolean turn = false;
	private boolean allowAim = true;
	
	private boolean showCue = false;
	private double cueAngle = 0.0, cueStartDist = 0.0;
	private boolean cueSet = false;
	private double cuePower = 0;
	private Ball cueBall;
	
	private List<Ball> balls;
	private GameInfo updateGI;
	
	private Client client;
	private GamePage gp;

	public Table(Client client, GamePage gp) {
		super(calculateRect(3*Window.dim.width/4));
		this.client = client;
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
	
	public void setUseCase(GameInfo gi, String id) {
		if(gi.practising) {
			tuc = TableUseCase.practicing;
			turn = true;
			
		} else if(gi.u1.id.equals(id) || gi.u2.id.equals(id)) {
			tuc = TableUseCase.playing;
			turn = gi.u1.id.equals(id);
			
		} else {
			tuc = TableUseCase.spectating;
			turn = false;
		}
	}

	@Override
	public void tick() {
		tickUpdate();
		aim();
		checkNewAim();
		
		for(Ball b: balls) {
			b.tick();
			
		} for(Ball b: balls) {
			b.update();
			
		}
	}
	
	private void tickUpdate() {
		if(updateGI != null) {
			this.balls = new ArrayList<>();
			this.turn = client.udata.id.equals(updateGI.turn);
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
		if(tuc == TableUseCase.playing || tuc == TableUseCase.practicing) {
			showCue = (tuc == TableUseCase.practicing || turn) && allowAim;
			
			if(showCue) {
				if(!cueSet) {
					Pointf xy = getMouseOnTable();
					cueAngle = getAngle(new Pointf(cueBall.x, cueBall.y), xy);
	
					// If the user wants this angle
					if(Client.mouse.left && Client.mouse.forleft < 2) {
						cueSet = true;
						cueStartDist = Func.getDis(xy.x, xy.y, cueBall.x, cueBall.y);
					}
					
				} else if(Client.mouse.left) {
					Pointf xy = getMouseOnTable();
					double angle = getAngle(new Pointf(cueBall.x, cueBall.y), xy);
					double distance = Func.getDis(xy.x, xy.y, cueBall.x, cueBall.y);
					
					cuePower = cueStartDist - distance;
					
				} else if(!Client.mouse.left && cuePower > 2.5) {
					shoot();
					
				} else {
					cuePower = 0.0;
					cueStartDist = 0.0;
					cueSet = false;
				}
			}
		}
	}
	
	private Pointf getMouseOnTable() {
		// Returns the mouse's XY on the table
		Point p = Client.mouse.getXY();
		return toTable(new Pointf(p.x-rect[0], p.y-rect[1]));
		
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
		double[] vel = Func.getVelocity(cueAngle, cuePower);
		cueBall.vx = vel[0];
		cueBall.vy = vel[1];
		
		turn = tuc != TableUseCase.playing;
		
		Packet p = getUpdate();
		client.server.send(p);
		
		allowAim = false;
		
	}
	
	private void checkNewAim() {
		if(turn && tuc != TableUseCase.spectating && !allowAim) {
			System.out.println("checking");
			boolean newAim = true;
			double threshold = 1;
			for(Ball b: balls) {
				if(Func.magnitude(b.vx, b.vy) > threshold) {
					newAim = false;

				} else {
					b.vx = 0.0;
					b.vy = 0.0;
				}
			} if(newAim) {
				allowAim = true;
				System.out.println(client.udata.info.username);
				
			} else {
				allowAim = false;
				
			}
		}
	}
	
	
	public void update(GameInfo gi) {
		this.updateGI = gi;
		
	}
	
	public Packet getUpdate() {
		Packet p = new Packet(Header.updateGame);
		p.gameInfo = gp.info;
		p.gameInfo.balls = getCircles();
		p.gameInfo.turn = turn ? gp.info.id : gp.info.id;
		
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
		int buffer = 64; // the buffer allows the balls going into pockets to be rendered
		
		// The ballsImg is 2048x1024, so it needs to be scaled down to bdim
		BufferedImage ballsImg = getBallsImage(buffer);
		g.drawImage(ballsImg, (int)bxy.x-buffer, (int)bxy.y-buffer, (int)bdim.width+buffer*2, (int)bdim.height+buffer*2, null);

		// Boundary, TODO remove later
		g.setColor(Color.ORANGE);
		g.drawRect((int)bxy.x, (int)bxy.y, (int)bdim.width, (int)bdim.height);
	}
	
	private BufferedImage getBallsImage(int buffer) {
		int scaledBuffer = (int) (buffer * (tdim.width / bdim.width));
		
		BufferedImage img = new BufferedImage(tdim.width + scaledBuffer*2, tdim.height + scaledBuffer*2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		for(Ball b: balls) {
			b.render(g, scaledBuffer);
		}
		
		return img;
		
	}
	
	private void renderCue(Graphics2D g) {
		if(showCue) {
			double projectionLength = 384;
			double cueLength = 256;
			double offset = Ball.DEFAULT_BALL_RADIUS*1.5;
			int thickness = 4;
			
			Pointf cueft = fromTable(new Pointf(cueBall.x, cueBall.y));
			cueft.x += rect[0];
			cueft.y += rect[1];
			
			
			double[] start = Func.getProjection(cueAngle, cuePower + offset, cueft);
			double[] end = Func.getProjection(cueAngle, cuePower + projectionLength + offset, cueft);
			
			g.setStroke(new BasicStroke(thickness));
			g.setColor(Color.GRAY);
			g.drawLine((int)start[0], (int)start[1], (int)end[0], (int)end[1]);
			
			
			start = Func.getProjection(cueAngle, cuePower + offset, cueft);
			end = Func.getProjection(cueAngle, cuePower + cueLength + offset, cueft);
			
			g.setColor(Color.YELLOW);
			g.drawLine((int)start[0], (int)start[1], (int)end[0], (int)end[1]);
			
		}
	}
	
	public Pointf toTable(Pointf p) {
		return new Pointf((p.x - bxy.x) * (tdim.width / bdim.width),
						(p.y - bxy.y) * (tdim.height / bdim.height));
	}

	public Pointf fromTable(Pointf p) {
		return new Pointf(p.x * (bdim.width / tdim.width) + bxy.x,
						p.y * (bdim.height / tdim.height) + bxy.y);
	}

}
