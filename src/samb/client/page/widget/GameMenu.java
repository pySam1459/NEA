package samb.client.page.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import samb.client.game.GamePage;
import samb.client.main.Client;
import samb.client.utils.Consts;
import samb.com.server.info.GameInfo;

public class GameMenu extends Widget {

	private final Color BACKGROUND_COLOR = new Color(64, 81, 77, 127);
	private final Color BORDER_COLOR = new Color(58, 75, 72, 200);
	
	private GamePage gp;
	//private GameInfo gi;
	
	public GameMenu(int[] rect, GamePage gp) {
		super(rect);
		
		this.gp = gp;
		initMenuWidgets();

	}
	
	private void initMenuWidgets() {
		int titleSize = 64, buffer=4;
		Text title1 = new Text("", new int[] {rect[0], rect[1], rect[2], titleSize},
				Consts.INTER.deriveFont(Font.PLAIN, titleSize), Consts.PAL1);
		title1.HIDDEN = true;
		title1.CENTERED = true;
		gp.add("title1", title1);
		
		Text title2 = new Text("", new int[] {rect[0], rect[1]+titleSize+buffer, rect[2], titleSize},
				Consts.INTER.deriveFont(Font.PLAIN, titleSize), Consts.PAL1);
		title2.HIDDEN = true;
		title2.CENTERED = true;
		gp.add("title2", title2);
		
	}

	@Override
	public void tick() {
		
		super.animTick();
	}
	
	public void setInfo(GameInfo gi) {
		//this.gi = gi;
		
		Text t;
		String[] titleIDs = new String[] {"title1", "title2"};
		switch(gi.tuc) {
		case playing:
			if(gi.u1.id.equals(Client.getClient().udata.id)) {
				titleIDs = new String[] {"title1", "title2"};
			} else {
				titleIDs = new String[] {"title2", "title1"};
			}
			// NOTE, no break here as using the next case's code
			
		case spectating:
			t = (Text) gp.get(titleIDs[0]);
			t.setText(gi.u1.username);
			t.HIDDEN = false;
			
			t = (Text) gp.get(titleIDs[1]);
			t.setText(gi.u2.username);
			t.HIDDEN = false;
			break;
			
		case practicing:
			t = (Text) gp.get("title1");
			t.setText(Client.getClient().udata.info.username);
			t.HIDDEN = false;
			break;
		}
	}

	
	@Override
	public void render(Graphics2D graph) {
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
		
		
	}
	
}
