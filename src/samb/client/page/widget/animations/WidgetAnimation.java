package samb.client.page.widget.animations;

import java.awt.Graphics2D;
import java.awt.Point;

import samb.client.page.widget.Widget;

public abstract class WidgetAnimation {
	/* This abstract class is the base for any widget animation
	 * A Widget will contain a list of WidgetAnimation objects
	 * The abstract methods allows a Widget object to interface with an animation without having to know which animation it is
	 * */
	
	protected int[] rect;
	
	protected Widget w;
	
	public WidgetAnimation(int[] rect) {
		this.rect = rect;
		
	} 
	
	public void setWidget(Widget w) {
		this.w = w;
		
	}
	
	public abstract void tick();
	
	public abstract void render(Graphics2D g);
	
	
	// Basic WidgetAnimation methods which different animations may use
	public boolean inRect(Point xy) {
		return rect[0] < xy.x && xy.x < rect[0] + rect[2] && rect[1] < xy.y && xy.y < rect[1] + rect[3];
		
	}
}
