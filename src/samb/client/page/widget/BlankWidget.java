package samb.client.page.widget;

import java.awt.Graphics2D;

public class BlankWidget extends Widget {

	public BlankWidget(int[] rect) {
		super(rect);

	}

	@Override
	public void tick() {
		super.animTick();
		
	}

	@Override
	public void render(Graphics2D g) {
		super.animRender(g);
		
	}

}
