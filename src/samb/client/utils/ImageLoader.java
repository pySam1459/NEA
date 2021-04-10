package samb.client.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import samb.client.main.Window;

public class ImageLoader {
	/* This class loads images into memory so they only have to be loaded once in one place
	 * This class uses static attributes and methods so that any image can be used anywhere in the program
	 * */
	
	private static HashMap<String, BufferedImage> images = new HashMap<>();
	
	public static BufferedImage get(String file) {
		if(!images.containsKey(file)) {
			try {
				images.put(file, ImageIO.read(new File("res/images/" + file)));
				
			} catch (IOException e) {
				System.out.println(file);
				e.printStackTrace();
			}
		}
		return images.get(file);
	}
	
	
	public static BufferedImage getBackground() {
		String file = String.format("background/%d.png", (int)Window.bgImgCounter);
		return get(file);
		
	}
	
	
	public void clearCache() {
		images.clear();
		
	}
}
