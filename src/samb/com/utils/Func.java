package samb.com.utils;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import samb.client.game.Ball;
import samb.client.utils.Consts;
import samb.client.utils.datatypes.Pointf;

public class Func {
	/* This class is a contains useful functions which can be called statically from either the host or client program */

	
	public static String hashPassword(String name, String password) {
		// This method hashes and salts a user's password
		// This algorithm is extra secure because 1. it uses SHA-256, but it also uses a modified version of the user's username as part of the salt
		// Therefore, an attacker would have to 1. know the algorithm and 2. know the modified username of a password, then recreate their whole dictionary, in order to perform a dictionary attack
		try {
			password = "£$asdf" + password + name.substring(1) + name.charAt(0) + "*(&";
			
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] data = md.digest(password.getBytes());
			BigInteger number = new BigInteger(1, data);
			
			return number.toString(16);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String copyChar(char c, int n) {
		// creates a string of repeated characters of length n
		
		String str = "";
		for(int i=0; i<n; i++) { str += c; }
		return str;
	}
	
	public static void loadFonts() {
		// Loads all non-system fonts from directory res/fonts
		try {
			Consts.INTER = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/Inter.ttf"));

		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static List<Circle> createDefaultBalls(Dimension dim, double r) {
		// Creates a list containing balls in their starting positions
		
		List<Circle> arr = new ArrayList<>();
		
		// 0-white, 1-red, 2-yellow, 3-black
		int[] cols = new int[] {1, 1, 2, 2, 3, 1, 1, 2, 1, 2, 2, 2, 1, 2, 1}; // Order of balls
		
		arr.add(new Circle(dim.width/4, dim.height/2, 0, 0, r, 0));
		
		double x = 3*dim.width/4 - r*6;
		double y;
		int count = 0;
		for(int j=0; j<5; j++) {
			x += Math.sqrt(3)/2 * (r*2+1);
			y = dim.height/2 - j*(r*2+1)/2;
			for(int i=0; i<j+1; i++) {
				arr.add(new Circle(x, y, r, cols[count]));
				y += r*2 +1;
				count++;
			}
		}

		return arr;
		
	}
	
	public static void collision(Ball b1, Ball b2) {
		// This method checks if ball b1 and ball b2 have collided
		// If they have, it will calculate the new velocities of each and update them
		
		if(ball2(b1, b2) && !b1.collidedWith.contains(b2)) {
			double di = getDis(b1.x, b1.y, b2.x, b2.y);
			//double di = Math.sqrt(Math.pow(b1.x - b2.x, 2) + Math.pow(b1.y - b2.y, 2));
			double nex = (b2.x - b1.x) / di;
			double ney = (b2.y - b1.y) / di;
			double p = 2 * (b1.vx * nex + b1.vy * ney - b2.vx * nex - b2.vy * ney) / 2;
			
			b1.vx = (b1.vx - p * nex) * Ball.BALL_FRICTION;
			b1.vy = (b1.vy - p * ney) * Ball.BALL_FRICTION;
			b2.vx = (b2.vx + p * nex) * Ball.BALL_FRICTION;
			b2.vy = (b2.vy + p * ney) * Ball.BALL_FRICTION;
			
			moveApart(b1, b2);
			b1.collidedWith.add(b2);
			b2.collidedWith.add(b1);
			
		}
	}
	
	public static void moveApart(Ball b1, Ball b2) {
		// Unlike in reality, when the 2 balls collide, they pass over eachother
		// Therefore, to correct this in simulations, we must separate them so that they don't pass over eachother
		
		double mx = (b1.x + b2.x) / 2;
		double my = (b1.y + b2.y) / 2;
		double d = getDis(b1.x, b1.y, b2.x, b2.y);
		
		b1.x = mx + b1.r * (b1.x - b2.x) / d;
		b1.y = my + b1.r * (b1.y - b2.y) / d;
		b2.x = mx + b2.r * (b2.x - b1.x) / d;
		b2.y = my + b2.r * (b2.y - b1.y) / d;
		
	}
	
	public static double[] getVelocity(double angle, double power) {
		double constant = 0.1;
		return new double[] {
				-power * Math.cos(angle) * constant,
				-power * Math.sin(angle) * constant
		};
	}
	
	public static double getDis(double x1, double y1, double x2, double y2) {
		// Returns the distance between 2 points
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
		
	}
	
	public static boolean ball2(Ball b1, Ball b2) {
		// Stands for ball squared, checks if 2 balls (circles) overlap
		return getDis(b1.x, b1.y, b2.x, b2.y) <= b1.r + b2.r;
	}
	
	public static double[] getProjection(double angle, double radius, Pointf origin) {
		return new double[] {
				origin.x + radius * Math.cos(angle),
				origin.y + radius * Math.sin(angle)
		};
	}
	
	public static double magnitude(double vx, double vy) {
		return Math.sqrt(vx*vx + vy*vy);
		
	}
	
}
