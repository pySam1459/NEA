package samb.client.page.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import samb.client.game.Ball;
import samb.client.game.GamePage;
import samb.client.main.Client;
import samb.client.page.widget.animations.LoadingDotsAnimation;
import samb.client.utils.Consts;
import samb.com.server.info.GameInfo;

public class GameMenu extends Widget {

	private final Color BACKGROUND_COLOR = new Color(64, 81, 77, 127);
	private final Color BORDER_COLOR = new Color(58, 75, 72, 200);
	private final BasicStroke SCORE_OUTLINE = new BasicStroke(3);
	
	public static final Color RED = new Color(254, 63, 32, 192);
	public static final Color YELLOW = new Color(255, 170, 0, 192);
	
	private GamePage gp;
	//private GameInfo gi;
	
	public GameMenu(int[] rect, GamePage gp) {
		super(rect);
		
		this.gp = gp;
		initMenuWidgets();

	}
	
	private void initMenuWidgets() {
		// This method initialises menu widgets
		
		int titleSize = 28, buffer=4, eloSize=16;
		Text title1 = new Text("???", new int[] {rect[0]+buffer*6, rect[1]+buffer*2, rect[2]-buffer*8, titleSize},
				Consts.INTER.deriveFont(Font.PLAIN, titleSize), Consts.PAL1);
		title1.HIDDEN = false;
		title1.CENTERED = false;
		gp.add("title1", title1);
		
		Text elo1 = new Text("", new int[] {rect[0]+buffer*2, rect[1]+buffer*2, rect[2]-buffer*8, titleSize}, 
				Consts.INTER.deriveFont(Font.PLAIN, eloSize), Consts.PAL1);
		elo1.HIDDEN = true;
		elo1.CENTERED = false;
		elo1.setAlign("right");
		gp.add("elo1", elo1);
		
		Text title2 = new Text("???", new int[] {rect[0]+buffer*6, rect[1]+titleSize+buffer*3, rect[2]-buffer*8, titleSize},
				Consts.INTER.deriveFont(Font.PLAIN, titleSize), Consts.PAL1);
		title2.HIDDEN = false;
		title2.CENTERED = false;
		gp.add("title2", title2);
		
		Text elo2 = new Text("", new int[] {rect[0]+buffer*2, rect[1]+titleSize+buffer*3, rect[2]-buffer*8, titleSize}, 
				Consts.INTER.deriveFont(Font.PLAIN, eloSize), Consts.PAL1);
		elo2.HIDDEN = true;
		elo2.CENTERED = false;
		elo2.setAlign("right");
		gp.add("elo2", elo2);
		
		Text pracTitle = new Text("Practice", new int[] {rect[0], rect[1], rect[2], titleSize*2},
				Consts.INTER.deriveFont(Font.PLAIN, titleSize), Consts.PAL1);
		pracTitle.HIDDEN = true;
		pracTitle.CENTERED = true;
		pracTitle.SHADOW = true;
		gp.add("pracTitle", pracTitle);
		
		BlankWidget loading = new BlankWidget(new int[] {rect[0]+buffer*6, rect[1]+titleSize+buffer*4,
														rect[2]/4, titleSize});
		loading.addAnimation(new LoadingDotsAnimation(loading.rect));
		gp.add("loading", loading);
		
	}

	@Override
	public void tick() {
		super.animTick();
	
	}
	
	public void setInfo(GameInfo gi) {
		// This method determines which widgets to show dependent on the TableUseCase, using the methods below
		
		unshowPlayers();
		unshowLoading();
		
		Text t;
		String[] titleIDs = new String[] {"title1", "title2", "elo1", "elo2"};
		switch(gi.tuc) {
		case playing:
			if(gi.u1.id.equals(Client.getClient().udata.id)) {
				titleIDs = new String[] {"title1", "title2", "elo1", "elo2"};
			} else {
				titleIDs = new String[] {"title2", "title1", "elo2", "elo1"};
			}
			// NOTE, no break here as using the next case's code
			
		case spectating:
			t = (Text) gp.get(titleIDs[0]);
			t.showText(gi.u1.username);
			t = (Text) gp.get(titleIDs[2]);
			t.showText(Integer.toString(gi.u1.elo));
			
			t = (Text) gp.get(titleIDs[1]);
			t.showText(gi.u2.username);
			t = (Text) gp.get(titleIDs[3]);
			t.showText(Integer.toString(gi.u2.elo));
			break;
			
		case practicing:
			showPractice();
			break;
		
		default:
			showPlayer1AsUsername();
			break;
			
		}
	}
	
	// These methods show different arrangements of title and elo widgets dependent on the TableUseCase
	public void unshowPlayers() {
		Text t = (Text) gp.get("title1");
		t.HIDDEN = true;
		t = (Text) gp.get("elo1");
		t.HIDDEN = true;
		
		t = (Text) gp.get("title2");
		t.HIDDEN = true;
		t = (Text) gp.get("elo2");
		t.HIDDEN = true;
		
	}
	
	public void showPlayers() {
		Text t = (Text) gp.get("title1");
		t.HIDDEN = false;
		t = (Text) gp.get("elo1");
		t.HIDDEN = false;
		
		t = (Text) gp.get("title2");
		t.HIDDEN = false;
		t = (Text) gp.get("elo2");
		t.HIDDEN = false;
	}
	
	
	public void showPlayer1AsUsername() {
		Text t = (Text) gp.get("title1");
		t.showText(Client.getClient().udata.userInfo.username);
		
		t = (Text) gp.get("elo1");
		t.showText(Integer.toString(Client.getClient().udata.userStats.elo));
		
	}
	
	public void showPractice() {
		Text t = (Text) gp.get("pracTitle");
		t.showText("Practicing");
		
		t = (Text) gp.get("title1");
		t.HIDDEN = true;
		t = (Text) gp.get("title2");
		t.HIDDEN = true;
		
		unshowLoading();
	}
	
	// These methods will show/unshow a loading animation, 3 dots which oscillate
	public void showLoading() {
		BlankWidget bw = (BlankWidget) gp.get("loading");
		bw.showAnim();
		
	}
	
	public void unshowLoading() {
		BlankWidget bw = (BlankWidget) gp.get("loading");
		bw.unshowAnim();
		
	}

	
	// Render Methods
	@Override
	public void render(Graphics2D graph) {
		// This method renders the game menu, excluding some widgets
		BufferedImage img = new BufferedImage(rect[2], rect[3], BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		int buffer = 8;
		
		g.setColor(BACKGROUND_COLOR);
		g.fillRoundRect(buffer/2, buffer/2, rect[2]-buffer, rect[3]-buffer, buffer, buffer);
		
		g.setStroke(new BasicStroke(2));
		g.setColor(BORDER_COLOR);
		g.drawRoundRect(buffer/2, buffer/2, rect[2]-buffer, rect[3]-buffer, buffer, buffer);
		
		renderScores(g);
		
		graph.drawImage(img, rect[0], rect[1], null);
		super.animRender(graph);

	}
	
	private void renderScores(Graphics2D g) {
		// Renders the scores of the players
		
		if(Client.getClient().udata.gameInfo != null) {
			int buffer = 8;
			int r = (int) (Ball.DEFAULT_BALL_RADIUS*0.8);
			
			Text t = (Text) gp.get("title2");
			int y = t.rect[1] + t.rect[3] + buffer*6;

			_renderIndividualScore(g, Client.getClient().udata.gameState.red, false, r, y, buffer, RED);
			_renderIndividualScore(g, Client.getClient().udata.gameState.yellow, false, r, y+r*2+buffer*2, buffer, YELLOW);
			
		}
	}
	
	private void _renderIndividualScore(Graphics2D g, int score, boolean black, int r, int y, int buffer, Color colour) {
		// Renders a player's score with given params
		g.setColor(colour);
		for(int i=0; i<7; i++) {
			if(i < score) {
				g.fillOval(buffer*2 + i*(r+buffer)*2, y-r, r*2, r*2);
			} else {
				g.setStroke(SCORE_OUTLINE);
				g.drawOval(buffer*2 + i*(r+buffer)*2, y-r, r*2, r*2);
			}
		}
		
		g.setColor(new Color(16, 16, 16, 192));
		if(black) {
			g.fillOval(buffer*2 + 7*(r+buffer)*2, y-r, r*2, r*2);
		} else {
			g.setStroke(SCORE_OUTLINE);
			g.drawOval(buffer*2 + 7*(r+buffer)*2, y-r, r*2, r*2);
		}
	}

}
