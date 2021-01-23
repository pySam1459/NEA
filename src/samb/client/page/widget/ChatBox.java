package samb.client.page.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ChatBox extends Widget {
	
	private final Color BACKGROUND_COLOR = new Color(64, 81, 77, 127);
	private final Color BORDER_COLOR = new Color(58, 75, 72, 200);

	public ChatBox(int[] rect) {
		super(rect);


	}

	@Override
	public void tick() {
		super.animTick();

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
		
		
		graph.drawImage(img, rect[0], rect[1], null);
		super.animRender(graph);
	}

}
