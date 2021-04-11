package samb.client.page;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import samb.client.main.Client;
import samb.client.main.Window;
import samb.client.page.widget.Button;
import samb.client.page.widget.Text;
import samb.client.page.widget.TextBox;
import samb.client.page.widget.animations.BoxFocusAnimation;
import samb.client.page.widget.listeners.ButtonListener;
import samb.client.utils.Consts;
import samb.client.utils.ImageLoader;
import samb.com.server.info.LoginInfo;
import samb.com.server.packet.Error;
import samb.com.server.packet.Header;
import samb.com.server.packet.Packet;
import samb.com.utils.Func;

public class LoginPage extends Page implements ButtonListener {
	/* This class is a subclass of Page class, which handles everything to do with the login Page
	 * It initializes all widgets seen on the login page and ticks/renders them
	 * It also handles the login and sign up processes, when the buttons are pressed, a packet is sent to the host,
	 * either to authorize or signup to the host.
	 * */
	
	private TextBox lUsername, lPassword;
	private Text lInvDets;
	private Button lButton;
	
	private TextBox sUsername, sEmail, sPassword, sRePassword;
	private Text sInvUsername, sInvEmail, sInvPassword, sInvDets;
	private Button sButton;
	
	public LoginPage() {
		super("LoginPage");
		
		initLogin();
		initSignUp();

	}
	
	
	private void initLogin() {
		// This method initializes all of the widgets for Login In side
		// A widget object is instantiated and any extra attributes are assigned
		// The widgets object will then be added to the page and the ticking/rendering will be handled by the Page super class
		
		add("LoginTitle", new Text("Login In", new int[] {Window.dim.width/4, 64, Window.dim.width/4, 128}, 
				Consts.INTER.deriveFont(Font.PLAIN, 72), Consts.PAL1));
		
		int buffer = 16;
		int w=512, h=72;
		int xoff = Window.dim.width/2-w-buffer*3, yoff=256;
		lUsername = new TextBox(new int[] {xoff, yoff, w, h}, "Username");   // Specific TextBox widget is created and parameters are passed
		lUsername.addAnimation(new BoxFocusAnimation(lUsername.rect, true)); // BoxFocusAnimation is applied to the widget
		add("LoginUsername", lUsername);                                     // Widget is added to the page
		
		lPassword = new TextBox(new int[] {xoff, yoff+h+buffer*2, w, h}, "Password");
		lPassword.HIDE_CHARS = true;                                                    // The password shouldn't be seen, so chars are hidden
		lPassword.addAnimation(new BoxFocusAnimation(lPassword.rect, true));
		add("LoginPassword", lPassword);
		
		lButton = new Button(new int[] {xoff+w/2-w/3, yoff+h*4+buffer*11, (int)(w/1.5), 96}, "Login In");
		lButton.addListener(this);
		lButton.addAnimation(new BoxFocusAnimation(lButton.rect));
		add("LoginInBut", lButton);
		
		lInvDets = new Text("Invalid Details", new int[] {xoff+w/2-w/3, yoff+h*4+buffer*17+4, (int)(w/1.5), 24}, new Font("Bahnschrift Light", Font.BOLD, 18), Consts.INVALID_COLOUR);
		lInvDets.HIDDEN = true;                                               // Only shown if the details are invalid
		add("LoginInvalidDets", lInvDets);
		
	}
	
	private void initSignUp() {
		// This method initializes all the widgets for the signup side
		
		add("SignUpTitle", new Text("Sign Up", new int[] {Window.dim.width/2, 64, Window.dim.width/4, 128}, 
				Consts.INTER.deriveFont(Font.PLAIN, 72), Consts.PAL1));
		
		int buffer = 16;
		int w=512, h=72;
		int xoff = Window.dim.width/2+buffer*3, yoff=256;
		sUsername = new TextBox(new int[] {xoff, yoff, w, h}, "Username");
		sUsername.charLimit = 20;
		sUsername.addAnimation(new BoxFocusAnimation(sUsername.rect, true));
		add("SignupUsername", sUsername);
		
		sInvUsername = new Text("Username is already taken", new int[] {xoff+buffer, yoff+h-4, w, 24}, 
				new Font("Bahnschrift Light", Font.BOLD, 18), Consts.INVALID_COLOUR);
		sInvUsername.HIDDEN = true;
		sInvUsername.CENTERED = false;
		add("SignupInvalidUsername", sInvUsername);
		
		sEmail = new TextBox(new int[] {xoff, yoff+h+buffer*2, w, h}, "Email");
		sEmail.addAnimation(new BoxFocusAnimation(sEmail.rect, true));
		add("SignupEmail", sEmail);
		
		sInvEmail = new Text("Invalid Email", new int[] {xoff+buffer, yoff+h*2+buffer*2-4, w, 24}, 
				new Font("Bahnschrift Light", Font.BOLD, 18), Consts.INVALID_COLOUR);
		sInvEmail.HIDDEN = true;
		sInvEmail.CENTERED = false;
		add("SignupInvalidEmail", sInvEmail);
		
		sPassword = new TextBox(new int[] {xoff, yoff+h*2+buffer*4, w, h}, "Password");
		sPassword.HIDE_CHARS = true;
		sPassword.addAnimation(new BoxFocusAnimation(sPassword.rect, true));
		add("SignupPassword", sPassword);
		
		sRePassword = new TextBox(new int[] {xoff, yoff+h*3+buffer*6, w, h}, "Re-type Password");
		sRePassword.HIDE_CHARS = true;
		sRePassword.addAnimation(new BoxFocusAnimation(sRePassword.rect, true));
		add("SignupRePassword", sRePassword);
		
		sInvPassword = new Text("Passwords are different", new int[] {xoff+buffer, yoff+h*4+buffer*6-4, w, 24}, 
				new Font("Bahnschrift Light", Font.BOLD, 18), Consts.INVALID_COLOUR);
		sInvPassword.HIDDEN = true;
		sInvPassword.CENTERED = false;
		add("SignupInvalidPasswords", sInvPassword);
				
		sButton = new Button(new int[] {xoff+w/2-w/3, yoff+h*4+buffer*11, (int)(w/1.5), 96}, "Sign Up");
		sButton.addListener(this);
		sButton.addAnimation(new BoxFocusAnimation(sButton.rect));
		add("SignUpBut", sButton);
		
		sInvDets = new Text("Invalid Details", new int[] {xoff, yoff+h*4+buffer*17+4, w, 24}, 
				new Font("Bahnschrift Light", Font.BOLD, 18), Consts.INVALID_COLOUR);
		sInvDets.HIDDEN = true;
		add("SignupInvalidDets", sInvDets);
		
	}
	
	@Override
	public void onClick(Button b) {
		// This method is called by either button when clicked
		
		if("LoginInBut".equals(b.id)) {
			login();
			
		} else if("SignUpBut".equals(b.id)) {
			signup();
			
		}
	}
	
	private void login() {
		// Before asking the host for authorization, we first check whether the details provided are valid
		lInvDets.HIDDEN = true;
		
		boolean valid = true;
		if(lUsername.getText().length() == 0 || lPassword.getText().length() == 0) {
			lInvDets.setText("Invalid Details");
			lInvDets.HIDDEN = false;
			valid = false;
		} else {
			lInvDets.HIDDEN = true;
		}
		
		if(valid) {
			// Here we send a packet to the host server with our login details (hashed and salted password)
			Packet p = new Packet(Header.login);
			p.loginInfo = new LoginInfo(lUsername.getText(), Func.hashPassword(lUsername.getText(), lPassword.getText()));
			
			Client.getClient().server.send(p);

		}
	}
	
	private void signup() {
		// Before signing up, we validate the details provided, checking password is re-typed corrects, email is formatted correctly, all fields are filled in
		
		sInvUsername.HIDDEN = true;
		sInvEmail.HIDDEN = true;
		sInvPassword.HIDDEN = true;
		sInvDets.HIDDEN = true;
		
		boolean valid = true;
		if(!sEmail.getText().matches("\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\b")) {
			sInvEmail.setText("Invalid Email");
			sInvEmail.HIDDEN = false;
			valid = false;
		} else {
			sInvEmail.HIDDEN = true;
		}
		if(!sPassword.getText().equals(sRePassword.getText()) ) {
			sInvPassword.setText("Passwords are different");
			sInvPassword.HIDDEN = false;
			valid = false;
		} else {
			sInvPassword.HIDDEN = true;
		}
		if(sUsername.getText().length() == 0 || sEmail.getText().length() == 0 ||
				sPassword.getText().length() == 0 || sRePassword.getText().length() == 0) {
			sInvDets.setText("Invalid Details");
			sInvDets.HIDDEN = false;
			valid = false;
		} else {
			sInvDets.HIDDEN = true;
		}
		
		if(valid) {
			// Here we send a packet to the host server with our sign up details
			Packet p = new Packet(Header.signup);
			p.loginInfo = new LoginInfo(sUsername.getText(), 
					sEmail.getText(), Func.hashPassword(sUsername.getText(), sPassword.getText()));
			
			Client.getClient().server.send(p);
			
		}
	}
	
	public void setError(Error err) {
		switch(err) {
		case invalidDetails:
			lInvDets.setText("Invalid Details");
			lInvDets.HIDDEN = false;
			break;
			
		case usernameTaken:
			sInvUsername.setText("Username is already taken");
			sInvUsername.HIDDEN = false;
			break;
			
		case emailTaken:
			sInvEmail.setText("Email is already taken");
			sInvEmail.HIDDEN = false;
			break;
			
		case alreadyOnline:
			lInvDets.setText("User Already Online!");
			lInvDets.HIDDEN = false;
			break;
			
		default:
			System.out.printf("An Error has occured!   =>   Error Token: %s\n", err.toString());
			break;
		}
	}
	
	@Override
	public void tick() {
		tickWidgets();
		getRender();
		
	}
	
	
	// Rendering
	@Override
	public BufferedImage getRender() {
		// A separate method is using for creating an image of the page so that transitions between pages can be programmed easier
		// The class variable 'img' is a Window.dim.width x Window.dim.height image which will be rendered
		
		Graphics2D g = getBlankCanvas();
		g.drawImage(ImageLoader.getBackground(), 0, 0, Window.dim.width, Window.dim.height, null);
		
		g.setColor(Consts.DARK_PAL1);
		g.setStroke(new BasicStroke(4));
		g.drawLine(Window.dim.width/2, 16, Window.dim.width/2, Window.dim.height-32);
		
		renderWidgets(g);  // renders widgets
		return img;
	}

	
	// Unused function
	@Override
	public void onRelease(Button b) {}

}
