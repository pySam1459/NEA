package samb.client.page.widget.animations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import samb.client.main.Client;
import samb.client.utils.Consts;

public class UnderLineAnimation extends WidgetAnimation {
	/* This animation shows a highlighting underline to a widget 
	 *   (normally a button or textBox)
	 * */
	
	private boolean hover = false;
	private int prog = 0;
	
	public int speed = 10;
	public int WIDTH = 2, BUFFER=6;
	public Color COLOUR = Consts.PAL1;

	public UnderLineAnimation(int[] rect) {
		super(rect);

	}

	@Override
	public void tick() {
		// either increases or decreases the alpha of the line, if hover or not
		hover = inRect(Client.getMouse().getXY());
		if(hover) {
			if(prog < 100) {
				prog += speed; // increases alpha of underline
				
				if(prog > 100) {
					prog = 100;
				}
			}
		} else {
			if(prog > 0) {
				prog -= speed; // decreases alpha of underline
				
				if(prog < 0) {
					prog = 0;
				}
			}
		}
	}

	@Override
	public void render(Graphics2D g) {
		// renders the line
		if(prog > 0) {
			g.setColor(new Color(COLOUR.getRed(), COLOUR.getGreen(), COLOUR.getBlue(), (int)(255.0*(prog/100.0))));
			g.setStroke(new BasicStroke(WIDTH));
			g.drawLine(rect[0]+BUFFER, rect[1]+rect[3]-BUFFER, rect[0]+rect[2]-BUFFER, rect[1]+rect[3]-BUFFER);
		}
	}

}
