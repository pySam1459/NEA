package samb.client.page.widget.animations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import samb.client.main.Client;

public class HoverShineAnimation extends WidgetAnimation {
	/* This subclass animation displays a shine animation when the user hovers over the widget
	 * */
	
	private final Color SHINE_COLOUR = new Color(227, 251, 247, 64);
	
	private boolean hover = false;
	private int progress = 0, xoff = 64, progmax;
	public int stroke = 48, speed = 6;
	
	private BufferedImage img;

	public HoverShineAnimation(int[] rect) {
		super(rect);

		this.progmax = xoff * 100 / rect[2] + 100;
		System.out.println(progmax);
	}

	@Override
	public void tick() {
		hover = inRect(Client.mouse.getXY());
		
		if((hover && progress < progmax) || (!hover && progress > 0)) {
			progress += speed;
			
			if(!hover && progress > progmax) {
				progress = 0;
			}
		}
	}

	@Override
	public void render(Graphics2D graph) {
		if(progress > 0 && progress < progmax) {
			img = new BufferedImage(rect[2], rect[3], BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) img.getGraphics();
			
			g.setColor(SHINE_COLOUR);
			g.setStroke(new BasicStroke(stroke));
			g.drawLine(rect[2]*progress/100, 0, rect[2]*progress/100 - xoff, rect[3]);
			
			graph.drawImage(img, rect[0], rect[1], null);
		}
	}

}
