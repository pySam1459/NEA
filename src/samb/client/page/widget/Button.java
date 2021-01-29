package samb.client.page.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import samb.client.main.Client;
import samb.client.page.widget.animations.BoxFocusAnimation;

public class Button extends Widget {
	/* This class is a subclass of the Widget class
	 * This class functions as a button, displaying a button and 
	 *  interfacing with the ButtonListeners when the user clicks and releases the button
	 * 
	 * */
	
	public Color BACKGROUND_COLOR = new Color(24, 231, 204, 64);
	public final Color SHADOW_COLOUR = new Color(0, 128, 132, 164);
	public final Color HOLD_COLOR = new Color(48, 244, 232, 156);
	public final Color FONT_COLOR = new Color(217, 241, 237, 225);
	
	public boolean hover=false, held=false;
	private List<ButtonListener> bls;
	
	private TextInfo ti;
	private Dimension tiDim;
	
	private BufferedImage background_image;
	
	public Button(int[] rect, String text, BufferedImage img) {
		super(rect);
		
		this.bls = new ArrayList<>();
		
		if(text != null) {
			this.ti = new TextInfo(text, new Font("Bahnschrift Light", Font.PLAIN, (int)(rect[3]*0.5)), FONT_COLOR);
			this.tiDim = ti.calculateDims(-1);
			resizeText();
			
		} if(img != null) {
			this.background_image = img;
		}
		
		addAnimation(new BoxFocusAnimation(rect));
	}
	
	private void resizeText() {
		// This method auto resizes the text in a button (if its too long)
		int s = ti.getSize();
		while(tiDim.width > rect[2]) {
			ti.setSize(--s);
			
		}
	}

	@Override
	public void tick() {
		checkHover();
		checkRelease();
		checkPress();
		
		super.animTick();
	}
	
	private void checkHover() {
		hover = inRect(Client.mouse.getXY());

	}
	
	private void checkPress() {
		// This method checks whether the user has pressed the button, if so it called the onClick method for each ButtonListener
		
		if(hover) {
			if(Client.mouse.left) {
				if(!held) {
					for(ButtonListener bl: bls) {
						bl.onClick(this);
						
					}
				}
				held = true;
				return;
			}
		}
		held = false;
	}
	
	private void checkRelease() {
		// This method checks whether the user has released the button,
		// if so it called the onRelease method for each ButtonListener
		
		if(hover && held) {
			if(!Client.mouse.left && Client.mouse.prevLeft) {
				held = false;
				for(ButtonListener bl: bls) {
					bl.onRelease(this);
					
				}
			}
		}
	}

	@Override
	public void render(Graphics2D graph) {
		// Renders the Button Widget
		
		if(!HIDDEN) {
			BufferedImage img = new BufferedImage(rect[2], rect[3], BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) img.getGraphics();
			
			g.setColor(!held ? BACKGROUND_COLOR : HOLD_COLOR);
			g.fillRect(0, 0, rect[2], rect[3]);
			
			int thickness = 6;
			g.setColor(SHADOW_COLOUR);
			g.fillRect(0, rect[3]-thickness, rect[2], rect[3]);
	
			if(img != null) {
				g.drawImage(background_image, 0, 0, rect[2], rect[3], null);
				
			} if(ti != null) {
				ti.render(g, new Point(rect[2]/2-tiDim.width/2, rect[3]/2+tiDim.height/2));
				
			}
			
			graph.drawImage(img, rect[0], rect[1], rect[2], rect[3], null);
			
			super.animRender(graph);
		}
	}
	
	public void addListener(ButtonListener bl) {
		bls.add(bl);
		
	}
	
	public void setText(String txt) {
		this.ti.setText(txt);
		
	}

}
