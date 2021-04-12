package samb.host.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import samb.com.database.UserInfo;
import samb.com.utils.Func;

public class UserDBManager {
	/* This class contains static methods which are used to query and update the 'users' table in 'OnlinePoolGame' mysql database which is hosted locally
	 * This class allows the Host to interact with the database and add, remove, select data to/from the database
	 * Each user has a row containing their unique id, username, email and password
	 * */
	
	private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
	private static final String connectionURL = "jdbc:mysql://localhost:3306/OnlinePoolGame?"
			+ "useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	
	public static final int[] MAX_UI_LENGTHS = new int[] {36, 20, 64, 64};
	
	private static Connection conn;
	
	// Initialisation methods
	public static void start() {
		try {
			initDatabase();
			connectToDatabase();
			
		} catch(SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static void initDatabase() throws ClassNotFoundException {
		// This method specifies to the DriverManager which JDBC driver to use when making Connections
		Class.forName(DRIVER);
		
	}
	
	private static void connectToDatabase() throws SQLException {
		// This method creates the connection to the database using the credentials specified.

		try {
			// uses credentials from file 'misc/res/sqlCredentials.cred'
			conn = DriverManager.getConnection(connectionURL, LoginCredentials.getUsername(), LoginCredentials.getPassword());
		
		} catch(Exception e) {
			System.out.println("Error connecting to Database");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	// Query Methods
	public static UserInfo getUI(String id) {
		// Returns a UserInfo object of a user with users.id = id;
		
		String query = String.format("SELECT * FROM users WHERE id='%s';", id);
		List<UserInfo> results = executeQuery(query);
		return results.size() == 0 ? null : results.get(0); // returns null if there is no user with id=$id in the table
	}
	
	public static UserInfo getUIFromName(String name) {
		String query = String.format("SELECT * FROM users WHERE username='%s';", name);
		List<UserInfo> results = executeQuery(query);
		return results.size() == 0 ? null : results.get(0);
		
	}
	
	public static List<UserInfo> getAll() {
		return executeQuery("SELECT * FROM users;");
		
	}
	
	public static boolean getOnline(String id) {
		// Returns if a user is online or not
		UserInfo ui = getUI(id);
		return ui != null ? ui.online : null;
	}
	
	public static void display(String orderby) {
		// This method displays the all rows in the users table
		
		// Gets all UserInfo ordered by argument orderby
		String query = String.format("SELECT * FROM users ORDER BY %s;", orderby);  
		List<UserInfo> arr = executeQuery(query);
		
		String[] columnNames = new String[] {"Username", "Email", "ID", "Password", "Online", "In Game"};
		int[] maxChars = new int[columnNames.length]; // max size of each column
		for(int i=0; i<columnNames.length; i++) { maxChars[i] = columnNames[i].length(); };
		
		// get largest lengths for each column, so can space table nicely
		for(UserInfo ui: arr) { _compareChars(ui, maxChars); } 
		String border = _createBorders(maxChars); // top and bottom edges of displayed table
		
		System.out.println(border);
		_displayRow(columnNames, maxChars);
		System.out.println(border);
		
		for(UserInfo ui: arr) {
			_displayUserInfo(ui, maxChars);
		}
		
		System.out.println(border);	
	}
	
	public static boolean exists(String field, String data) {
		String query = String.format("SELECT * FROM users WHERE %s='%s';", field, data);
		return executeQuery(query).size() > 0;
	}

	
	// Update Methods
	public static boolean createTable() {
		// This method creates the table in the 'online pool game' database
		String update = "CREATE TABLE users (id VARCHAR(36) PRIMARY KEY,"
										  + "username VARCHAR(20),"
										  + "email VARCHAR(64),"
										  + "password VARCHAR(64),"
										  + "online BOOLEAN,"
										  + "inGame BOOLEAN);";  
		// The passwords kept in the database are hashed (SHA-256) and salted
		return executeUpdate(update);
	}
	
	public static boolean dropTable() {
		String update = "DROP TABLE users;";
		return executeUpdate(update);
		
	}
	
	public static boolean addUser(UserInfo ui) {
		// This method adds a UserInfo object to the table
		String update = String.format("INSERT INTO users VALUES ('%s', '%s', '%s', '%s', FALSE, FALSE);", 
				ui.id, ui.username, ui.email, ui.password);
		return executeUpdate(update);
	}

	public static boolean removeUser(String id) {
		String update = String.format("DELETE FROM users WHERE id='%s';", id);
		return executeUpdate(update);
	}
	
	public static boolean exists(String name) {
		UserInfo ui = getUIFromName(name);
		return ui != null;
		
	}
	
	// Setters
	public static boolean setOnline(String id, boolean online) {
		String update = String.format("UPDATE users SET online=%b WHERE id='%s';", online, id);
		return executeUpdate(update);
	}
	
	public static boolean setIngame(String id, boolean inGame) {
		String update = String.format("UPDATE users SET inGame=%b WHERE id='%s';", inGame, id);
		return executeUpdate(update);
	}
	
	public static boolean setAllOffline() {
		return executeUpdate("UPDATE users SET online=false AND inGame=false;");
	}
	
	
	// General Methods
	public static boolean executeUpdate(String update) {
		// This method is a general SQL Update; to be used by any other method which wants query an update
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(update);
			stmt.close();
			return true;
			
		} catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static List<UserInfo> executeQuery(String query) {
		// This method returns the results from a SQL query to the database
		// The results must be parsed into usable data, ie a list of UserInfo objects
		try {
			Statement stmt = conn.createStatement();
			
			ResultSet results = stmt.executeQuery(query);
			List<UserInfo> data = parseResults(results);
			
			stmt.close();
			return data;
			
		} catch(SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static List<UserInfo> parseResults(ResultSet results) throws SQLException {
		/* A java SQL Query returns a ResultSet object, which contains the data from that query
		 * To parse this data, you must loop through the set and create a UserInfo object for each row in the set
		 * To Note, SQL indexing starts at 1 */
		
		List<UserInfo> data = new ArrayList<>();
		UserInfo ui;
		while(results.next()) {
			ui = new UserInfo(results.getString(1), results.getString(2), results.getString(3), 
					results.getString(4), results.getBoolean(5), results.getBoolean(6));
			data.add(ui);	
		}
		
		return data;
	}
	
	public static void close() {
		try {
			// Attempts (normally successful) to close the database connection
			conn.close();
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	// Methods used in the 'display' method
	private static void _compareChars(UserInfo ui, int[] maxChars) {
		// Compares the lengths of each attribute in the UserInfo object to the current maximum char length
		// attributes: username, email, id, password, online, inGame
		
		if(ui.username.length() > maxChars[0]) { maxChars[0] = ui.username.length(); }
		if(ui.email.length() > maxChars[1]) { maxChars[1] = ui.email.length(); }
		if(ui.id.length() > maxChars[2]) { maxChars[2] = ui.id.length(); }
		if(ui.password.length() > maxChars[3]) { maxChars[3] = ui.password.length(); }
		if((ui.online?4:5) > maxChars[4]) {maxChars[4] = ui.online?4:5;} // {True: False}
		if((ui.inGame?4:5) > maxChars[5]) {maxChars[5] = ui.inGame?4:5;} // {True: False}
		
	}
	
	private static String _createBorders(int[] maxChars) {
		String border = "+";
		for(int n: maxChars) {
			border += Func.copyChar('-', n+1) + "+";
		}
		return border;
	}
	
	private static void _displayRow(String[] row, int[] maxChars) {
		String str = "|";
		for(int i=0; i<row.length; i++) {
			str += row[i] + Func.copyChar(' ', maxChars[i] - row[i].length() +1) + "|";
		}
		System.out.println(str);
	}
	
	private static void _displayUserInfo(UserInfo ui, int[] maxChars) {
		_displayRow(new String[] {ui.username, ui.email, ui.id, ui.password, 
				Boolean.toString(ui.online), Boolean.toString(ui.inGame)}, maxChars);
		
	}
	
}
