package samb.client.page.widget;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import samb.client.main.Client;
import samb.client.utils.Consts;
import samb.com.database.UserInfo;
import samb.com.server.info.Win;
import samb.com.utils.enums.TableUseCase;

public class EndScreen extends Widget {
	/* This widget is shown at the end of a game, showing the results of the game
	 *   and giving the option to return to the menu
	 * */
	
	private BufferedImage img;
	private float opacity = 0.0f, maxOpacity = 0.85f;
	private long timer = 0, revealPeriod = 8;
	private int deltaElo = 0;
	
	public TableUseCase tuc = TableUseCase.playing;
	private UserInfo winner, loser;
	
	private Font titleFont, smallTitleFont, byWhatFont, eloDeltaFont;
	private Button retoBut;

	public EndScreen(int[] rect, Button retoBut) {
		super(rect);
		this.retoBut = retoBut;
	
		// All fonts derive from INTER
		this.titleFont = Consts.INTER.deriveFont(Font.PLAIN, rect[3]/7);
		this.smallTitleFont = Consts.INTER.deriveFont(Font.PLAIN, rect[3]/10);
		this.byWhatFont = Consts.INTER.deriveFont(Font.PLAIN, rect[3]/12);
		this.eloDeltaFont = Consts.INTER.deriveFont(Font.PLAIN, rect[3]/12);
	}
	

	@Override
	public void tick() {
		if(!HIDDEN && opacity < maxOpacity) {
			// This widget will appear at the end of the match, 
			//   by revealing itself as its opacity increases
			if(System.currentTimeMillis() - timer >= revealPeriod) {
				opacity += 0.02f;
				timer = System.currentTimeMillis();
				
			} if(opacity > maxOpacity) {
				opacity = maxOpacity;
			}
		} 
	}
	
	public void reveal(Win win, boolean amWinner) {
		// This method starts the reveal process for the endScreen widget
		
		createImg(win, amWinner);
		HIDDEN = false;
		timer = System.currentTimeMillis();
	}
	
	public void reveal(Win win, UserInfo u1, UserInfo u2, String wId) {
		// This method starts the reveal process for a spectator, 
		//   shows change in elo for both players
		this.winner = u1.id.equals(wId) ? u1 : u2;
		this.loser = u1.id.equals(wId) ? u2 : u1;
		
		createImg(win, false);
		HIDDEN = false;
		timer = System.currentTimeMillis();
	}
	
	private void createImg(Win win, boolean amWinner) {
		// This method creates the endScreen widget image, called a few times as possible
		
		img = new BufferedImage(rect[2], rect[3], BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		// Background
		int arc = 32;
		g.setColor(Consts.MENU_BACKGROUND_COLOR);
		g.fillRoundRect(2, 2, rect[2]-4, rect[3]-4, arc, arc);
		
		g.setColor(Consts.MENU_BORDER_COLOR);
		g.setStroke(new BasicStroke(2));
		g.drawRoundRect(2, 2, rect[2]-4, rect[3]-4, arc, arc);
		
		// Render title ("You Won!", "You Lost")
		TextInfo ti = getTitleTextInfo(amWinner);
		int yoff = rect[3]/4+ti.dim.height/2;
		Point xy = new Point(rect[2]/2 - ti.dim.width/2, yoff);
		ti.render(g, xy);
		
		// By what
		ti = new TextInfo(byWhat(win), byWhatFont, Consts.PAL1);
		xy = new Point(rect[2]/2 - ti.dim.width/2, yoff + ti.dim.height+8);
		ti.render(g, xy);
		yoff += ti.dim.height+8;
		
		// Delta Elo
		if(tuc != TableUseCase.spectating) {
			ti = getEloDeltaTextInfo(Client.getClient().udata.userStats.elo, deltaElo);
			xy = new Point(rect[2]/2 - ti.dim.width/2, yoff + ti.dim.height+32);
			ti.render(g, xy);
			
		} else {
			ti = getEloDeltaTextInfo(winner.elo, deltaElo);
			xy = new Point(rect[2]/2 - ti.dim.width-4, yoff + ti.dim.height+32);
			ti.render(g, xy);
			
			ti = getEloDeltaTextInfo(loser.elo, -deltaElo);
			xy = new Point(rect[2]/2+4, yoff + ti.dim.height+32);
			ti.render(g, xy);
		}
	}
	
	private TextInfo getTitleTextInfo(boolean amWinner) {
		// Return the Title of the endScreen, dependent on tuc
		switch(tuc) {
		case playing:
			return new TextInfo(amWinner ? "You Won!" : "You Lost", 
					titleFont, amWinner ? Color.GREEN : Color.RED);
		case practicing:
			return new TextInfo("Practice Finished", smallTitleFont, Consts.PALE);
		case spectating:
			return new TextInfo(winner.username + " Won!", titleFont, Color.GREEN);
		}
		return null;
	}
	
	private TextInfo getEloDeltaTextInfo(int cElo, int dElo) {
		// Return the change in elo textInfo
		String text;
		Color col;
		if(dElo > 0) {
			text = Integer.toString(cElo) + " +" + Integer.toString(dElo);
			col = Color.GREEN;
		} else if(dElo < 0) {
			text = Integer.toString(cElo) + " " + Integer.toString(dElo);
			col = Color.RED;
		} else { // practice
			text = Integer.toString(cElo);
			col = Consts.PALE;
		}
		return new TextInfo(text, eloDeltaFont, col);
	}
	
	private String byWhat(Win win) {
		// Returns the 'by What' text based on the 'win'
		switch(win) {
		case pottedBlack: 
			return "By Potting The 8 Ball";
		case pottedAll: 
			return "By Potting All Balls";
		case forfeit: 
			return "By Forfeit";
		default: 
			return "";
		}
	}

	@Override
	public void render(Graphics2D g) {
		// Renders the endScreen
		if(!HIDDEN) {
			if(img != null) {
				// Sets the transparency of the endScreen to $opacity 0f->1f
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
				
				g.drawImage(img, rect[0], rect[1], null);
				retoBut.render(g);
				
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			}
		}
	}

	public void setDeltaElo(int delo) {
		this.deltaElo = delo;
	}
	
}
