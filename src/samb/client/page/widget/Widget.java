package samb.client.page.widget;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import samb.client.page.widget.animations.WidgetAnimation;

public abstract class Widget {
	/* This abstract class acts as a base for all widgets, so common attributes ("id", "rect", etc) can be stored here
	 * This reduces complexity and code in the subclasses and allows for subclasses to be stored under 1 type "Widget"
	 * */
	
	public String id;
	public int[] rect;
	
	protected boolean HIDDEN = false;
	
	protected List<WidgetAnimation> anims;
	
	public Widget(int[] rect) {
		this.rect = rect;
		
		this.anims = new ArrayList<>();
		
	}
	
	public abstract void tick();
	
	protected void animTick() {
		for(WidgetAnimation wa: anims) {
			wa.tick();
			
		}
	}
	
	public abstract void render(Graphics2D g);
	
	protected void animRender(Graphics2D g) {
		for(WidgetAnimation wa: anims) {
			wa.render(g);
			
		}
	}
	
	public void addAnimation(WidgetAnimation wa) {
		wa.setWidget(this);
		anims.add(wa);
		
	}
	
	public boolean inRect(Point xy) {
		return rect[0] < xy.x && xy.x < rect[0] + rect[2] && rect[1] < xy.y && xy.y < rect[1] + rect[3];
		
	}

}
