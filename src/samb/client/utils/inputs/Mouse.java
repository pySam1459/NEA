package samb.client.utils.inputs;

import java.awt.IllegalComponentStateException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import samb.client.main.Client;

public class Mouse implements MouseListener {
	/* This object handles mouse events from the window
	 * The Window object will call either the mousePressed or mouseReleased methods when relevant
	 * */
	
	public boolean left=false, right=false, prevLeft=false, prevRight=false, justButton=false;
	public int forleft=0, forright=0;
	private Point p1, p2;
	
	public Mouse() {
		Client.getWindow().addMouseListener(this);
		
	}

	// Interface methods
	@Override
	public void mousePressed(MouseEvent m) {
		if(m.getButton() == MouseEvent.BUTTON1) {
			left = true;
		} else if(m.getButton() == MouseEvent.BUTTON3) {
			right = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent m) {
		if(m.getButton() == MouseEvent.BUTTON1) {
			left = false;
		} else if(m.getButton() == MouseEvent.BUTTON3) {
			right = false;
		}
	}
	
	// Returns a point on the screen
	public Point getXY() {
		// This method returns the current (x, y) coords of the mouse in the window 
		try {
			p1 = Client.getWindow().getLocationOnScreen();
			p2 = MouseInfo.getPointerInfo().getLocation();
			return new Point(p2.x-p1.x, p2.y-p1.y);
			
		} catch(IllegalComponentStateException e) {
			return new Point(0, 0);
		}
	}
	
	public void update() {
		// This method updates the 'prev' variables at the end of a tick
		// These variables can be used to determine if the user has just clicked/released the mouse
		this.prevLeft = left;
		this.prevRight = right;
		
		if(left) {
			forleft++;
		} else {
			forleft = 0;
			
		} if(right) {
			forright++;
		} else {
			forright = 0;
		}
	}

	
	// These methods are not used
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	
}
