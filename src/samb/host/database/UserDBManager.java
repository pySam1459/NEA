package samb.host.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import samb.com.database.UserInfo;

public class UserDBManager {
	/* This class contains static methods which are used to query and update the 'users' table in 'online pool game' mysql database which is hosted on the locahost
	 * This class allows the Host to interact with the database and add, remove, select data to/from the database
	 * Each user has a row containing their unique id, username, email and password
	 * */
	
	private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
	private static final String connectionURL = "jdbc:mysql://localhost:3306/OnlinePoolGame";
	
	// These details below are to be changed to your username and password for your mysql database
	private static final String USERNAME="admin", PASSWORD="password";
	
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
		// Realistically the Admin credentials shouldn't be hard-coded, but this program isn't meant to be used commercially, so it doesn't particularly matter
		
		conn = DriverManager.getConnection(connectionURL, USERNAME, PASSWORD);
		
	}
	
	// Query Methods
	public static UserInfo getUI(String id) {
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
	
	public static void display(String orderby) {
		// This method displays 
		
		String query = String.format("SELECT * FROM users ORDER BY %s;", orderby);  // Gets all UserInfo ordered by argument orderby
		List<UserInfo> arr = executeQuery(query);
		
		String[] columnNames = new String[] {"Username", "Email", "ID", "Password"};  // column names
		int[] maxChars = new int[columnNames.length];  
		for(int i=0; i<columnNames.length; i++) { maxChars[i] = columnNames[i].length(); };
		
		for(UserInfo ui: arr) { _compareChars(ui, maxChars); } // get largest lengths for each column, so can space table nicely
		String border = _createBorders(maxChars);              // top and bottom edges of displayed table
		
		System.out.println(border);
		_displayRow(columnNames, maxChars);
		System.out.println(border);
		for(UserInfo ui: arr) {
			_displayUserInfo(ui, maxChars);
		}
		
		System.out.println(border);
		
	}
	
	
	// Update Methods
	public static boolean createTable() {
		// This method creates the table in the 'online pool game' database
		String update = "CREATE TABLE users (id VARCHAR(36) PRIMARY KEY,"
										  + "username VARCHAR(20),"
										  + "email VARCHAR(64),"
										  + "password VARCHAR(64));";  // The passwords kept in the db will have been hashed (SHA-256) and salted
		//return executeUpdate(update);
		
		// Testing purposes
		executeUpdate(update);
		return true;
	}
	
	public static boolean dropTable() {
		String update = "DROP TABLE users;";
		return executeUpdate(update);
		
	}
	
	public static boolean resetTable() {
		// This method is mainly used for testing purposes
		
		if(dropTable()) {
			return createTable();
		} 
		return false;
	}
	
	public static boolean addUser(UserInfo ui) {
		String update = String.format("INSERT INTO users VALUES ('%s', '%s', '%s', '%s');", ui.id, ui.username, ui.email, ui.password);
		return executeUpdate(update);
	}
	
	public static boolean updateUsername(UserInfo ui) {
		String update = String.format("UPDATE users SET username='%s' WHERE id='%s';", ui.username, ui.id);
		return executeUpdate(update);
	}
	
	public static boolean updateEmail(UserInfo ui) {
		String update = String.format("UPDATE users SET email='%s' WHERE id='%s';", ui.username, ui.id);
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
			ui = new UserInfo(results.getString(1), results.getString(2), results.getString(3), results.getString(4));
			data.add(ui);
			
		}
		
		return data;
	}
	
	public static void close() {
		try {
			conn.close();
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	// Methods used in the 'display' method
	
	private static void _compareChars(UserInfo ui, int[] maxChars) {  // "username", "email", "id", "password"
		if(ui.username.length() > maxChars[0]) { maxChars[0] = ui.username.length(); }
		if(ui.email.length() > maxChars[1]) { maxChars[1] = ui.email.length(); }
		if(ui.id.length() > maxChars[2]) { maxChars[2] = ui.id.length(); }
		if(ui.password.length() > maxChars[3]) { maxChars[3] = ui.password.length(); }
		
	}
	
	private static String _createBorders(int[] maxChars) {
		String border = "+";
		for(int n: maxChars) {
			border += _copyChar('-', n+1) + "+";
		}
		return border;
	}
	
	private static void _displayRow(String[] row, int[] maxChars) {
		String str = "|";
		for(int i=0; i<row.length; i++) {
			str += row[i] + _copyChar(' ', maxChars[i] - row[i].length() +1) + "|";
		}
		System.out.println(str);
	}
	
	private static void _displayUserInfo(UserInfo ui, int[] maxChars) {
		_displayRow(new String[] {ui.username, ui.email, ui.id, ui.password}, maxChars);
		
	}
	
	private static String _copyChar(char c, int n) {
		String str = "";
		for(int i=0; i<n; i++) { str += c; }
		return str;
	}
	
}
