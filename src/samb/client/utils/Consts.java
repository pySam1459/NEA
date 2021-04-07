package samb.client.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Consts {
	/* This class contains constant static class variables which can be used throughout the program
	 * */
	
	// Colours
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
	
	// Fonts
	public static Font INTER;
	
	// this object is used when getting the length of rendered text of a particular font
	public static final Graphics2D fmg = (Graphics2D) new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics();
	
	
	// Maths Constants
	// These constants are hard coded as they have been fine tuned to give a good playing experience
	public static final double DT_CONST = 800.0;        // dt = DT_CONST / TPS
	public static final double VELOCITY_POWER = 0.0175; // constant multiplied by cue.power when calculating velocity
	public static final int FINE_TUNE_ITERS = 50;       // number of smaller iterations of ball movement
	
	public static final double TABLE_FRICTION = 0.01; // Friction applied to ball per tick of movement
	public static final double BALL_FRICTION = 0.97;    // Friction applied to ball in a collision with a ball
	public static final double CUSHION_FRICTION = 0.95; // Friction applied to ball in a collision with a cushion
	public static final double SPEED_THRESHOLD = 0.35;  // Lowest |V| a ball can have to be 'moving'

	
	// Rendering constant objects
	public static final BasicStroke cueStroke = new BasicStroke(4);
	public static final BasicStroke cueProjectionStroke = new BasicStroke(2);
	
	
	// Dev section, TODO remove in release
	public static boolean DEV_SHOW_MOUSE_POS = false;
	
}
