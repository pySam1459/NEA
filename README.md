# Non Exam Assessment

My NEA Project is an Online Pool Game, a Java application with a custom everything library.
The Project is based around a Client-Server Architecture, contains 2 programs: the **Host** and the **Client**.


## Host

The Host is the server which the User logins onto and requests certain actions.
The server uses the UDP protocol, so there is a chance that a packet may be lost in the post. 

#### Terminal
The Host is controlled via a terminal where an admin can issue commands to interact with the program.
The Command format is ``$group $cmd $arg1 $arg2 ...``, example  ``server start 2001``
Groups, CMDs, arguments:
* server
	* start [PORT]
	* stop
	
* database / db
	* create [TABLE_NAME]
	* delete [TABLE_NAME]
	* setup
	* remove [USERNAME]
	* display [TABLE_NAME, ORDERBY]
	* details [USERNAME]
	
* game
	* start [USERNAME_1, USERNAME_2]
	* stop [USERNAME]
	* spectate [USERNAME_1, USERNAME_2]
	
* misc (not specified)
	* exit
	* quit

#### Database
The Host uses a MySQL database
To setup this database,
1. Create a file 'res/misc/sqlCredentials.cred', which contains the credentials as such:

	```
	username
	password
	```
	
2.Run the sql script: mysqlSetup.sql, (can be run by):

	```bash
	mysql source scripts/mysqlSetup.sql
	```

3. When the Host program is run for the first time, the command below should be run:

	```
	db setup
	```
	
At the end of the day, a mysql database called 'OnlinePoolGame' should be created, 
and the credentials to access this database should be stored in a file 'res/misc/sqlCredentials.sql'

## Client

The Client is the program run by an end-User, who interacts with a Graphical User Interface.
The program uses a *Page* system, where each page contains *Widgets*, 
  giving the user the ability to interact with the program.
  
#### Features
To Play against a random user, press the 'Join Pool' button and wait for a match
To Practice Pool, press the 'Practice' button

If you want to play against a friend, first search for their username in the search bar,
then click their name and press the 'Add Friend' button.
If they are online, you can challenge them to a game of Pool.
Or if they are already in a game, you can spectate their game.

#### ELO
A User's ELO is a rating system for their ability at Pool.
This value will increase after a win and decrease after a loss.
This magnitude of this increase is based of the tanh(x) function, where x is the difference in the players' elo