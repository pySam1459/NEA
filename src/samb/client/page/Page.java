package samb.client.page;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import samb.client.main.Window;
import samb.client.page.widget.Widget;

public abstract class Page {
	/* This abstract class will be used as a basis for all other pages in the program
	 * A HashMap contains all of the widgets which will be on the page
	 * */
	
	public String id;
	protected BufferedImage img;
	
	protected HashMap<String, Widget> widgets;

	public Page(String id) {
		this.id = id;
		this.widgets = new HashMap<>();
		
	}
	
	public void add(String id, Widget w) {
		// This method adds a widget to the page
		w.id = id;
		widgets.put(id, w);
		
	}
	
	public Widget get(String id) {
		return widgets.get(id);
		
	}
	
	public Graphics2D getBlankCanvas() {
		// This method first creates a new image to draw onto (to be rendered)
		// secondly, it returns a Graphics2D object, which is the blank canvas, to be drawn on (hence the name)
		
		img = new BufferedImage(Window.dim.width, Window.dim.height, BufferedImage.TYPE_INT_ARGB);
		return (Graphics2D) img.getGraphics();
	}
	
	// This abstract method is required by each page to tick the widgets and get a render
	public abstract void tick();
	
	// This abstract method is where the page will be drawn onto an image, to be rendered
	// This method returns the rendered page image as it will be used during page transitions
	public abstract BufferedImage getRender();
	
	
	// These methods iterate through the widgets, either ticking or rendering them
	public void tickWidgets() {
		widgets.forEach((String id, Widget w) -> {
			w.tick();
			
		});
	}
	
	public void renderWidgets(Graphics2D g) {
		widgets.forEach((String id, Widget w) -> {
			w.render(g);
			
		});
	}
	
}
