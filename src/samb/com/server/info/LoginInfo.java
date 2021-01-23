package samb.com.server.info;

import java.io.Serializable;

import samb.com.server.packet.Error;

public class LoginInfo implements Serializable {
	/* This class contains information which will be used in the login/sign up process
	 * This class will be serialised and sent across the Internet
	 * */
	
	private static final long serialVersionUID = 1252886540048258492L;
	public String username, email, password;
	public boolean authorized = false;
	public Error err;
	
	public LoginInfo() {}
	
	public LoginInfo(String username, String password) { // For Login
		this.username = username;
		this.password = password;
		
	}

	public LoginInfo(String username, String email, String password) { // For Sign up
		this.username = username;
		this.email = email;
		this.password = password;
		
	}
	
}
