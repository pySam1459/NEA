package samb.host.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import samb.com.database.Friend;
import samb.host.game.UserManager;


public class FriendsDBManager {
	/* This class contains static methods which are used to query and update the Friends Tables in 'OnlinePoolGame' mysql database which is hosted locally
	 * This class allows the Host to interact with the database, create and drop tables, add and remove friends from each table, and get friends of a user
	 * Each row in a table contains the id of a friend
	 * */
	
	private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
	private static final String connectionURL = "jdbc:mysql://localhost:3306/OnlinePoolGame";

	private static Connection conn;
	private static UserManager um;
	
	// Initialisation methods
	public static void start(UserManager um) {
		try {
			initDatabase();
			connectToDatabase();
			FriendsDBManager.um = um;
			
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
		conn = DriverManager.getConnection(connectionURL, LoginCredentials.username, LoginCredentials.password);

	}
	
	// Query Methods
	/* Unfortunately, to get all the relevant information about the friends requires querying the Users table for every friend,
	*  and checking with the UserManager.isOnline for every friend.
	*  This seems like it could be a bottleneck if there are lots of users, but for now its acceptable
	*/
	public static List<Friend> getAll(String id) {
		String query = String.format("SELECT * FROM Friends_%s;", id);
		List<Friend> friends = executeQuery(query);
		
		for(Friend f: friends) {
			f.online = um.isOnline(id);
			f.username = UserDBManager.getUI(id).username;
		}
		return friends;
		
	}
	
	public static List<Friend> getAllOnline(String id) {
		String query = String.format("SELECT * FROM Friends_%s;", id);
		List<Friend> friends = executeQuery(query);
		List<Friend> onlineFriends = new ArrayList<>();
		
		for(Friend f: friends) {
			f.online = um.isOnline(id);
			if(f.online) {
				f.username = UserDBManager.getUI(id).username;				
				onlineFriends.add(f);
				
			}
		}
		return onlineFriends;
	}
	
	
	// Update Methods
	public static boolean addUser(String id) {
		String update = String.format("CREATE TABLE Friends_%s (id VARCHAR(36) PRIMARY KEY);", id);
		return executeUpdate(update);
		
	}
	
	public static boolean removeUser(String id) {
		String update = String.format("DROP TABLE Friends_%s;", id);
		return executeUpdate(update);
		
	}
	
	public static boolean addFriend(String uId, String fId) {
		String update = String.format("INSERT INTO Friends_%s VALUES ('%s');", uId, fId);
		return executeUpdate(update);
		
	}
	
	public static boolean removeFriend(String uId, String fId) {
		String update = String.format("DELETE FROM Friends_%s WHERE id='%s';", uId, fId);
		return executeUpdate(update);
		
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
		// The results must be parsed into usable data, ie a list of UserInfo objects
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
		List<Friend> data = new ArrayList<>();
		Friend f;
		while(results.next()) {
			f = new Friend(results.getString(1));
			data.add(f);
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
	
	
}
