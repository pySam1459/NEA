package samb.client.page.widget;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import samb.client.utils.Consts;
import samb.com.server.info.Win;

public class EndScreen extends Widget {
	/* This widget is shown at the end of a game, showing the results of the game
	 * */
	
	private BufferedImage img;
	private float opacity = 0.0f, maxOpacity = 0.85f;
	private long timer = 0, revealPeriod = 8;
	
	private Font titleFont, byWhatFont;
	private Button retoBut;

	public EndScreen(int[] rect, Button retoBut) {
		super(rect);
		this.retoBut = retoBut;
	
		this.titleFont = new Font("Inter", Font.PLAIN, rect[3]/7);
		this.byWhatFont = new Font("Inter", Font.PLAIN, rect[3]/12);
	}
	

	@Override
	public void tick() {
		if(!HIDDEN && opacity < maxOpacity) {
			// This widget will appear at the end of the match, by revealing itself as its opacity increases
			if(System.currentTimeMillis() - timer >= revealPeriod) {
				opacity += 0.02f;
				timer = System.currentTimeMillis();
				
			} if(opacity > maxOpacity) {
				opacity = maxOpacity;
			}
		} 
	}
	
	public void reveal(Win win, boolean amWinner) {
		createImg(win, amWinner);
		HIDDEN = false;
		timer = System.currentTimeMillis();
	
	}
	
	private void createImg(Win win, boolean amWinner) {
		img = new BufferedImage(rect[2], rect[3], BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		// Background
		int arc = 32;
		g.setColor(GameMenu.BACKGROUND_COLOR);
		g.fillRoundRect(2, 2, rect[2]-4, rect[3]-4, arc, arc);
		
		g.setColor(GameMenu.BORDER_COLOR);
		g.setStroke(new BasicStroke(2));
		g.drawRoundRect(2, 2, rect[2]-4, rect[3]-4, arc, arc);
		
		// Render title ("You Won!", "You Lost")
		TextInfo ti = new TextInfo(amWinner ? "You Won!" : "You Lost", titleFont, amWinner ? Color.GREEN : Color.RED);
		int titleY = rect[3]/4+ti.dim.height/2;
		Point xy = new Point(rect[2]/2 - ti.dim.width/2, titleY);
		ti.render(g, xy);
		
		// By what
		ti = new TextInfo(byWhat(win), byWhatFont, Consts.PAL1);
		xy = new Point(rect[2]/2 - ti.dim.width/2, titleY + ti.dim.height+4);
		ti.render(g, xy);
		
		//setOpacity(0.0f);
	}
	
	private String byWhat(Win win) {
		switch(win) {
		case pottedBlack: 
			return "By Potting The Black Early";
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
		if(!HIDDEN) {
			if(img != null) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
				
				g.drawImage(img, rect[0], rect[1], null);
				retoBut.render(g);
				
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			}
		}
	}

}
