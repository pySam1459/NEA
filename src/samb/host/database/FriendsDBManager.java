package samb.host.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import samb.com.database.Friend;
import samb.com.database.UserInfo;

public class FriendsDBManager {
	/* This class contains static methods which are used to query and update the Friend Tables 
	 *   in 'OnlinePoolGame' mysql database
	 * This class allows the Host to query and update the database and its tables
	 * A User has a 'Friends_$ID' table where each row is the id of a friend
	 * */
	
	private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
	private static final String connectionURL = "jdbc:mysql://localhost:3306/OnlinePoolGame?"
			+ "useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

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
		conn = DriverManager.getConnection(connectionURL, LoginCredentials.getUsername(), LoginCredentials.getPassword());

	}
	
	// Query Methods
	public static Friend get(String uId, String fId) {
		// Returns a Friend object of a user with id=fId, if they are a friend
		String tName = "Friends_" + clean(uId);  // Selecting, friend id, username, online and inGame status
		String query = String.format("SELECT id, username, online, inGame FROM users "
								   + "WHERE users.id = '%s' AND users.id IN "
								   + "(SELECT id FROM %s);", fId, tName);

		List<Friend> results = executeQuery(query);
		return results.size() > 0 ? results.get(0) : null;
	}
	
	public static List<Friend> getAll(String id) {
		// Returns a list containing all a user's friends
		String tName = "Friends_" + clean(id);  // Selecting, friend id, friend username, and friend online status
		String query = String.format("SELECT %s.id, users.username, users.online, users.inGame, TRUE "
								   + "FROM %s "
								   + "JOIN users ON %s.id = users.id;", tName, tName, tName);
		return executeQuery(query);
		
	}
	
	public static List<Friend> findFriends(String uId, String search) {
		// This method attempts to find new friends based on a search argument
		String tName = "Friends_" + clean(uId);
		String query = String.format("SELECT users.id, users.username, users.online, users.inGame, FALSE "
				+ "FROM users WHERE users.id NOT IN (SELECT id FROM %s) AND users.id != '%s' "
				+ "AND users.username LIKE '%s' LIMIT 10;", tName, uId, "%"+search+"%");
		return executeQuery(query);
	}

	
	// Update Methods
	public static boolean addUser(String id) {
		String update = String.format("CREATE TABLE Friends_%s (id VARCHAR(36) PRIMARY KEY);", clean(id));
		return executeUpdate(update);
	}
	
	public static boolean removeUser(String id) {
		String update = String.format("DROP TABLE Friends_%s;", clean(id));
		return executeUpdate(update);
	}
	
	public static boolean addFriend(String uId, String fId) {
		String update = String.format("INSERT INTO Friends_%s VALUES ('%s');", clean(uId), fId);
		return executeUpdate(update);
	}
	
	public static boolean removeFriend(String uId, String fId) {
		String update = String.format("DELETE FROM Friends_%s WHERE id='%s';", clean(uId), fId);
		return executeUpdate(update);
	}
	
	public static boolean createAll() {
		// Used by console commands: db create friends, db setup
		List<UserInfo> uis = UserDBManager.getAll();
		for(UserInfo ui: uis) {
			if(!addUser(ui.id)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean dropAll() {
		try {
			// This statement gathers all tables from information_schema.tables with the prefix = 'friends_'
			String query = "SELECT table_name FROM information_schema.tables "
						 + "WHERE table_schema='OnlinePoolGame' AND table_name LIKE 'friends_%';";
			Statement stmt = conn.createStatement();
			ResultSet results = stmt.executeQuery(query);
			
			while(results.next()) {
				String update = String.format("DROP TABLE %s;", results.getString(1));
				executeUpdate(update);
			}
			stmt.close();
			return true;
			
		} catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
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

	public static List<Friend> executeQuery(String query) {
		// This method returns the results from a SQL query to the database
		// The results must be parsed into usable data, ie a list of Friend objects
		try {
			Statement stmt = conn.createStatement();
			
			ResultSet results = stmt.executeQuery(query);
			List<Friend> data = parseResults(results);
			
			stmt.close();
			return data;
			
		} catch(SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static List<Friend> parseResults(ResultSet results) throws SQLException {
		// This method returns a list of friend ids of a user, gathered from a query
		
		List<Friend> data = new ArrayList<>();
		Friend f;
		while(results.next()) {
			f = new Friend(results.getString(1), results.getString(2), 
					results.getBoolean(3), results.getBoolean(4), results.getBoolean(5));
			data.add(f);
		}
		
		return data;
	}
	
	
	public static void close() {
		// Closes database connection
		try {
			conn.close();
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	private static String clean(String id) {
		return id.replace("-", "");
		
	}
	
}
