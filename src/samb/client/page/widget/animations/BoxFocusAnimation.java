package samb.client.page.widget.animations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import samb.client.main.Client;
import samb.client.utils.Consts;

public class BoxFocusAnimation extends WidgetAnimation {
	/* This class allows widgets to exhibit an animation where a box around the widget fades and focuses in and out when mouse is hovering over
	 * This class is a subclass of the WidgetAnimation class
	 * */
	
	private int focus = 100;
	private boolean hover = false, rounded=false;
	
	public int WIDTH = 3;
	public Color COLOUR = Consts.PAL1;

	
	public BoxFocusAnimation(int[] rect) {
		super(rect);

	}
	
	public BoxFocusAnimation(int[] rect, boolean rounded) {
		super(rect);
		this.rounded = rounded;  // if the box is rounded
		
	}

	@Override
	public void tick() {
		hover = inRect(Client.mouse.getXY());
		if(hover) {
			if(focus > 0) {
				focus -= 7; // shrinks the box
				if(focus < 0) {
					focus = 0;
				}
			}
		} else {
			if(focus != 100) {
				focus += 7;  // expands the box
				if(focus > 100) {
					focus = 100;
				}
			}
		}
	}

	@Override
	public void render(Graphics2D g) {
		if(focus != 100) {
			int alpha = 255*(100-focus)/100;
			g.setColor(new Color(COLOUR.getRed(), COLOUR.getGreen(), COLOUR.getBlue(), alpha));
			g.setStroke(new BasicStroke(WIDTH));
			
			double off = 8*focus/100;
			if(rounded) {
				int s = Math.min(rect[2], rect[3]);
				g.drawRoundRect(rect[0]-(int)off, rect[1]-(int)off, (int)(rect[2]+off*2), (int)(rect[3]+off*2), s/2, s/2);
			} else {
				g.drawRect(rect[0]-(int)off, rect[1]-(int)off, (int)(rect[2]+off*2), (int)(rect[3]+off*2));
			}
		}

	}

}
