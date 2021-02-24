package samb.client.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Consts {
	/* This class contains constant static class variables which can be used throughout the program
	 * */
	
	
	public static final Color BACKGROUND_COLOUR = new Color(41, 40, 37);
	public static final Color PALE = new Color(251, 245, 237, 200);
	public static final Color GREY_PALE = new Color(173, 168, 160, 200);
	public static final Color INVALID_COLOUR = new Color(224, 40, 37, 200);
	public static final Color SHADOW_COLOUR = new Color(48, 48, 48, 127);
	
	public static final Color PAL1 = new Color(227, 251, 247);
	public static final Color PAL2 = new Color(24, 231, 204);
	public static final Color PAL3 = new Color(0, 154, 159);
	public static final Color PAL4 = new Color(64, 81, 77);
	public static final Color DARK_PAL1 = new Color(184, 207, 203);
	
	public static Font INTER;
	
	// this object is used when getting the length of rendered text of a particular font
	public static final Graphics2D fmg = (Graphics2D) new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics();

	// Dev section, TODO remove in release
	public static boolean DEV_SHOW_MOUSE_POS = false;
	
}
