package samb.client.page.widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;

import samb.client.utils.Consts;

public class Text extends Widget {
	/* This class is a subclass of the Widget class
	 * This widget simply displays text onto the screen
	 * The text, font, colour and size will be passed in as constructor parameters
	 * */
	
	private TextInfo ti;
	public boolean CENTERED = true, HIDDEN=false, SHADOW=false;
	private String align = "left";

	public Text(String text, int[] rect, Font font, Color colour) {
		super(rect);

		this.ti = new TextInfo(text, font, colour);
	}

	@Override
	public void tick() {
		super.animTick();
		
	}
	
	
	// Setters
	public void setText(String txt) {
		this.ti.setText(txt);
		
	}
	
	public void showText(String txt) {
		setText(txt);
		HIDDEN = false;
	}
	
	public void setAlign(String side) {
		switch(side.toLowerCase().charAt(0)) {
		case 'l': 
			align = "left";
			break;
		case 'r':
			align = "right";
			break;
		default:
			break;
		}
	}
	
	public void setColour(Color colour) {
		ti.setColour(colour);
		
	}

	
	// Render Method
	@Override
	public void render(Graphics2D g) {
		// Renders the Text widget
		
		if(!HIDDEN) {
			Point xy;
			if(CENTERED) {
				xy = new Point(rect[0]+rect[2]/2-ti.dim.width/2, rect[1]+rect[3]-(rect[3]-ti.dim.height)/2);
			} else {
				if(align.equals("right")) {
					xy = new Point(rect[0]+rect[2] - ti.dim.width, rect[1]+rect[3]-(rect[3]-ti.dim.height)/2);
				} else {
					xy = new Point(rect[0], rect[1]+rect[3]-(rect[3]-ti.dim.height)/2);
				}
			}
			
			if(SHADOW) {
				ti.render(g, new Point(xy.x + 3, xy.y + 3), Consts.SHADOW_COLOUR);
			}
			
			ti.render(g, xy);
		
			super.animRender(g);
		}
	}

}
