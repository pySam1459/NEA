package samb.host.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import samb.com.database.UserInfo;
import samb.com.database.UserStats;
import samb.com.utils.Func;

public class StatsDBManager {
	/* This class is contains static methods which are used to query and update the 'stats' table 
	 *   in 'OnlinePoolGame' mysql database which is hosted locally
	 * This class allows the Host to interact with the database and add, remove, select data to/from the database
	 * Each user has a row containing their unique id, Elo, # Games, 
	 *   # Games Won, # Games Lost, # balls potted, highestElo, highestEloVictory
	 * */
	
	
	private static final String DRIVER = "com.mysql.cj.jdbc.Driver";	
	private static final String connectionURL = "jdbc:mysql://localhost:3306/OnlinePoolGame?"
			+ "useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	
	public static final int[] MAX_UI_LENGTHS = new int[] {};
	
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
	public static UserStats getUS(String id) {
		// Returns a UserStats object of the user with stats.id=id;
		String query = String.format("SELECT * FROM stats WHERE id='%s';", id);
		List<UserStats> results = executeQuery(query);
		return results.size() == 0 ? null : results.get(0); // returns null if there is no user with id=$id in the table
	}
	
	public static int getElo(String id) {
		return getUS(id).elo;
	}
	
	public static List<UserStats> getAll() {
		return executeQuery("SELECT * FROM stats;");
		
	}
	
	public static void display(String orderby) {
		// This method displays 
		
		orderby = _orderByCorrection(orderby);
		if(orderby == null) {
			System.out.println("Invalid Order Argument");
			return;
		}
		
		// Queries data to be displayed
		// Gets all UserStats ordered by argument orderby
		String query = String.format("SELECT stats.* FROM stats JOIN users ON users.id = stats.id ORDER BY %s;", orderby);  
		List<UserStats> arrUS = executeQuery(query);
		
		String query2 = String.format("SELECT users.* FROM users JOIN stats ON stats.id = users.id ORDER BY %s", orderby);
		List<UserInfo> arrUI = UserDBManager.executeQuery(query2);
		
		// Creates a list of max column lengths
		String[] columnNames = new String[] {"Username", "Elo", "# Games", "# Games Won", "# Games Lost", 
											 "# Balls Potted", "Highest Elo", "Highest Elo Victory"};  // column names
		int[] maxChars = new int[columnNames.length];  
		for(int i=0; i<columnNames.length; i++) { maxChars[i] = columnNames[i].length(); };
		
		// get largest lengths for each column, so can space table nicely
		for(int i=0; i<arrUS.size(); i++) { _compareChars(arrUS.get(i), arrUI.get(i), maxChars); } 
		String border = _createBorders(maxChars);  // top and bottom edges of displayed table
		
		// Outputs Table
		System.out.println(border);
		_displayRow(columnNames, maxChars);
		System.out.println(border);
		
		for(int i=0; i<arrUS.size(); i++) {
			_displayUserStats(arrUS.get(i), arrUI.get(i), maxChars);
		}
		
		System.out.println(border);
	}
	
	
	// Update Methods
	public static boolean createTable() {
		// This method creates the table in the 'online pool game' database
		String update = "CREATE TABLE stats (id VARCHAR(36), elo INT, noGames INT, noGamesWon INT, "
				+ "noGamesLost INT, noBallsPotted INT, highestElo INT, highestEloVictory INT)";
		executeUpdate(update);
		
		for(UserInfo ui: UserDBManager.getAll()) {
			addUser(new UserStats(ui.id));
		}
		
		return true;
	}
	
	public static boolean dropTable() {
		String update = "DROP TABLE stats;";
		return executeUpdate(update);
		
	}
	
	public static boolean addUser(UserStats us) {
		String update = String.format("INSERT INTO stats VALUES ('%s', %d, %d, %d, %d, %d, %d, %d);", 
				us.id, us.elo, us.noGames, us.noGamesWon, us.noGamesLost, us.noBallsPotted, us.highestElo, us.highestEloVictory);
		return executeUpdate(update);
	}

	public static boolean removeUser(String id) {
		String update = String.format("DELETE FROM stats WHERE id='%s';", id);
		return executeUpdate(update);
	}
	
	public static boolean setUS(String id, UserStats us) {
		if(removeUser(id)) {
			return addUser(us);
		}
		return false;
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

	public static List<UserStats> executeQuery(String query) {
		// This method returns the results from a SQL query to the database
		// The results must be parsed into usable data, ie a list of UserStats objects
		try {
			Statement stmt = conn.createStatement();
			
			ResultSet results = stmt.executeQuery(query);
			List<UserStats> data = parseResults(results);
			
			stmt.close();
			return data;
			
		} catch(SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static List<UserStats> parseResults(ResultSet results) throws SQLException {
		/* A java SQL Query returns a ResultSet object, which contains the data from that query
		 * To parse this data, you must loop through the set and create a UserStats object for each row in the set
		 * To Note, SQL indexing starts at 1 */
		List<UserStats> data = new ArrayList<>();
		UserStats ui;
		while(results.next()) {
			ui = new UserStats(results.getString(1), results.getInt(2), results.getInt(3), results.getInt(4), 
							   results.getInt(5), results.getInt(6), results.getInt(7), results.getInt(8));
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
	// These methods format the table layout and data
	private static void _compareChars(UserStats us, UserInfo ui, int[] maxChars) {
		if(ui.username.length() > maxChars[0]) { maxChars[0] = ui.username.length(); }
		
		int[] iUs = us.toArray();
		int l;
		for(int i=0; i<iUs.length; i++) {
			l = getIntLength(iUs[i]);
			if(l > maxChars[i+1]) {
				maxChars[i+1] = l;
				
			}
		}
	}
	
	private static int getIntLength(int n) {
		// Returns the number of digits of an integer
		return n == 0 ? 1 : (int)Math.log10(n)+1;
	}
	
	private static String _orderByCorrection(String orderby) {
		// specifies which table a column belongs to
		
		if(orderby.matches("(username|email).*")) {
			return "users." + orderby;
		} else if(orderby.matches("(elo|noGames|noGamesWon|noGamesLost|noBallsPotted|highestElo|highestEloVictory).*")) {
			return "stats." + orderby;
		} else if(orderby.matches("(id).*")) {
			return "users." + orderby;
		} else {
			return null;
		}
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
	
	private static void _displayUserStats(UserStats us, UserInfo ui, int[] maxChars) {
		int[] iUs = us.toArray();
		String[] sUs = new String[iUs.length];
		for(int i=0; i<iUs.length; i++) {
			sUs[i] = Integer.toString(iUs[i]);
		}
		
		_displayRow(new String[] {ui.username, sUs[0], sUs[1], sUs[2], sUs[3], sUs[4], sUs[5], sUs[6]}, maxChars);
		
	}
	
}