package samb.client.page.widget;

import java.awt.Graphics2D;

import samb.com.server.info.Win;

public class EndScreen extends Widget {
	/* This widget is shown at the end of a game, showing the results of the game
	 * */

	public EndScreen(int[] rect) {
		super(rect);

		this.HIDDEN = true;
	}
	

	@Override
	public void tick() {


	}
	
	public void reveal(Win win, String winner) {
		HIDDEN = false;
	
	}

	@Override
	public void render(Graphics2D g) {
		if(!HIDDEN) {
		
			
		}
	}

}
