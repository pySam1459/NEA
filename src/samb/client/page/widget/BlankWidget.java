package samb.client.page.widget;

import java.awt.Graphics2D;

public class BlankWidget extends Widget {
	
	private boolean showAnim = true;

	public BlankWidget(int[] rect) {
		super(rect);

	}

	@Override
	public void tick() {
		if(showAnim) {
			super.animTick();
		
		}
	}

	@Override
	public void render(Graphics2D g) {
		if(showAnim) {
			super.animRender(g);
		
		}
	}
	
	public void showAnim() {
		this.showAnim = true;
		
	}
	
	public void unshowAnim() {
		this.showAnim = false;
	}

}
