package samb.com.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Config {

	private static HashMap<String, Object> config;
	
	public static Object get(String key) {
		return config.get(key);
		
	}
	
	public static void loadConfig() {
		config = new HashMap<>();
		String configpath = "res/misc/config.cfg";
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(configpath)));
			String line;
			while((line = br.readLine()) != null) {
				parseLine(line);
				
			}
		} catch(IOException e) {
			System.out.println("config.cfg was unable to be read from");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private static void parseLine(String line) {
		String key="", value="";
		boolean keying = true;
		
		for(char c: line.replace(" ", "").toCharArray()) {
			if(c == '#') {
				break;
			} else if(c == '=' && keying) {
				keying = false;
			} else if(keying) {
				key += c;
			} else {
				value += c;
			}
		}
		
		if(!key.equals("") && !value.equals("")) {
			config.put(key, toObject(value));
		
		}
	}
	
	private static Object toObject(String value) {
		if(value.matches("\\d+")) {
			return Integer.parseInt(value);
			
		} else if(value.matches("\\d*\\.\\d+")) {
			return Double.parseDouble(value);
			
		} else if(value.matches("(true|false|TRUE|FALSE)")) {
			return value.equals("true") || value.equals("TRUE");
			
		} else {
			return value;
		}
	}
	
}
