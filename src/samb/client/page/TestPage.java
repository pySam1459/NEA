package samb.client.page;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import samb.client.page.widget.Button;
import samb.client.utils.Consts;


public class TestPage extends Page {
	
	
	public TestPage() {
		super("TestPage");
		//add("username", new TextBox(new int[] {64, 64, 384, 64}, "Username"));
		add("button1", new Button(new int[] {64, 64, 256, 128}, "Button1", null));
		add("button2", new Button(new int[] {64, 256, 256, 128}, "Button2", null));
		add("button3", new Button(new int[] {64, 448, 256, 128}, "Button3", null));
		add("button4", new Button(new int[] {64, 640, 256, 128}, "Button4", null));
		
	}
	
	@Override
	public void tick() {
		tickWidgets();
		getRender();
		
	}


	@Override
	public BufferedImage getRender() {
		Graphics2D g = getBlankCanvas();
		
		g.setColor(Consts.BACKGROUND_COLOUR);
		//g.setColor(Color.BLACK);
		g.fillRect(0, 0, dim.width, dim.height);
		
		renderWidgets(g);
		
		return img;
	}

}
