package samb.client.main;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

public class Window extends Canvas {
	/* This class is used to setup a window to render on
	 * The 'Graphics2D' class acts as a canvas for the program, 
	 * The Window class creates a Graphics2D object and passes it to any other class which renders something onto the screen
	 * Afterwards, the class then shows the Graphics2D, which is what is shown on screen
	 * */

	private static final long serialVersionUID = -8853826068392301075L;
	private static final String TITLE = "Online Pool Game";
	public static final Dimension dim = new Dimension(1920, 1080);
	
	public static double bgImgCounter = 1.0, bgImgDir=0.2;
	
	private BufferStrategy bs;
	private Graphics2D g;
	
	private JFrame frame;
	
	public Window() {
		this.frame = new JFrame(TITLE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize();
		frame.setResizable(false); // To allow the window to be resizable, it adds an extra layer of complexity which is unnecessary
		frame.add(this);
		
		// The 'windowClosing' method will be called by the listener if the window is closed, and so the program needs to close
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent w) {
				Client.getClient().stop();
				
			}
		});
		
	}
	
	public static void updateBackground() {
		bgImgCounter += bgImgDir;
		if(bgImgCounter >= 255 || bgImgCounter <= 1) {
			bgImgDir *= -1;
		}
	}
	
	public Graphics2D getGraphics() {
		// This method will create a Graphics2D object and return it to be used throughout the program
		
		if(isVisible()) {
			bs = this.getBufferStrategy();
			if(bs == null) {
				this.createBufferStrategy(3);
				bs = this.getBufferStrategy();
			}
			
			g = (Graphics2D) bs.getDrawGraphics();
			
			return g;
		}
		return null;
	}
	
	public void render() {
		// After all render calls have been completed, the Graphics2D object will be shown on screen
		
		bs.show();
		g.dispose();
	}
	
	public void start() {
		frame.setVisible(true);
	}
	
	public void stop() {
		frame.setVisible(false);
	}
	
	private void setSize() {
		// Insets are the borders around a window, so when setting the frame size, you must account for the extra pixels
		Insets insets = frame.getInsets();
		frame.setSize(dim.width + insets.left + insets.right, dim.height + insets.top + insets.bottom);
		
	}

}
