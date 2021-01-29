package samb.client.page.widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;

public class Text extends Widget {
	/* This class is a subclass of the Widget class
	 * This widget simply displays text onto the screen
	 * The text, font, colour and size will be passed in as constructor parameters
	 * */
	
	private TextInfo ti;
	public boolean CENTERED = true, HIDDEN=false;

	public Text(String text, int[] rect, Font font, Color colour) {
		super(rect);

		this.ti = new TextInfo(text, font, colour);
	}

	@Override
	public void tick() {
		super.animTick();
		
	}
	
	public void setText(String txt) {
		this.ti.setText(txt);
		
	}

	@Override
	public void render(Graphics2D g) {
		// Renders the Text widget
		
		if(!HIDDEN) {
			Point xy;
			if(CENTERED) {
				xy = new Point(rect[0]+rect[2]/2-ti.dim.width/2, rect[1]+rect[3]-(rect[3]-ti.dim.height)/2);
			} else {
				xy = new Point(rect[0], rect[1]+rect[3]);
			}
			
			ti.render(g, xy);
			
			super.animRender(g);
		}
	}

}
