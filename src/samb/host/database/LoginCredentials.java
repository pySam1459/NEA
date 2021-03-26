package samb.host.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LoginCredentials {
	// These details below are to be changed to your username and password for your mysql database
	// These values are in a certain file so I can add this file to the .gitignore
	
	private static String username;
	private static String password;

	
	public static String getUsername() {
		return LoginCredentials.username;
	}
	
	public static String getPassword() {
		return LoginCredentials.password;
	}
	
	public static void loadCredentials() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("res/misc/sqlCredentials.cred")));
			String line;
			while((line = br.readLine()) != null) {
				if(username == null) {
					username = line;
				} else {
					password = line;
					br.close();
					return;
				}
			}
		} catch(IOException e) {
			System.out.println("Error loading SQL Database Credentials");
			System.exit(-1);
		}
	}
}
