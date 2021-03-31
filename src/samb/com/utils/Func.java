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

import samb.client.utils.Consts;

public class Func {
	/* This class is a contains useful functions which can be called statically from either the host or client program */

	// These FLAGS are used throughout the program, a username cannot equals any of these flags
	public static final String[] FLAGS = new String[] {"$BOLD$", "$ITALICS$", "$PLAIN$", "$BOLD NOSPACE$"};
	
	
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
			x += Math.sqrt(3)/2 * (r*2+4);
			y = dim.height/2 - j*(r*2+4)/2;
			for(int i=0; i<j+1; i++) {
				arr.add(new Circle(x, y, r, cols[count]));
				y += r*2 +1;
				count++;
			}
		}

		return arr;
		
	}
	
	public static boolean isFlag(String flag) {
		// Check whether 'flag' is a FLAG
		
		for(String s: FLAGS) {
			if(s.equals(flag)) {
				return true;
			}
		} return false;
	}
	
}
