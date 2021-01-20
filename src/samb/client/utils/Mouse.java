package samb.client.utils;

import java.awt.IllegalComponentStateException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import samb.client.main.Client;

public class Mouse implements MouseListener {
	
	public boolean left=false, right=false, prevLeft=false, prevRight=false;
	public int forleft=0, forright=0;
	private Point p1, p2;
	
	public Mouse() {
		Client.window.addMouseListener(this);
		
	}

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
	
	public Point getXY() {
		try {
			p1 = Client.window.getLocationOnScreen();
			p2 = MouseInfo.getPointerInfo().getLocation();
			return new Point(p2.x-p1.x, p2.y-p1.y);
			
		} catch(IllegalComponentStateException e) {
			return new Point(0, 0);
		}
	}
	
	public void update() {
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
