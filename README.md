# NEA

This is my Non-Exam Assessment source code.
My NEA project is an **Online Pool Game**.
This will be a desktop application and **NOT** a browser based game.


## Structure

The OPG is based around a client-server architecture, where the client and server are separate programs.
The **Host** (*ie the server*) is ran first and a command line is used to interact with the host program.
The **Client** (*ie a user*) is ran whenever a user wants to play pool.


### Client

The Client program is a GUI made from a custom widget library.
Each widget also has a list of WidgetAnimations which are rendered after and onto the widget.
The program uses a *Page* system, where a page contains widgets.
These pages can transition between to go from, *ie the menuPage to the gamePage*


### Host

The Host requires a login, which currently is:
* admin
* password

The Host Command Line is made from different command groups, and their commands [arguments]:
* server
	* start [PORT]
	* stop
	
* database / db
	* create [TABLE_NAME]
	* delete [TABLE_NAME]
	* reset [TABLE_NAME]
	* remove [USERNAME]
	* display [TABLE_NAME, ORDERBY]
	* details [USERNAME]
	
* game
	* start [USERNAME_1, USERNAME_2]
	* stop [USERNAME]
	
* misc (not specified)
	* exit
	* quit

To execute a command, run '$group $command $arg1 $arg2 ...'
Example: server start 1234
This example is used to start the host server, which is **required** before clients can send packets to the host.


#### Database
MySQL is the database of choice, and if you decide to run the host on your machine, you need to create your own '*OnlinePoolGame*' mysql database
There are currently 2 tables being used:
* users
	Stores the (id *KEY*, username, email, password)
* stats
	Stores the (id *KEY*, elo, noGames, noGamesWon, noGamesLost, noBallsPotted, highestElo, highestEloVictory)
	The users table must be created before the stats table, as the stats table fills with data from the users table

In the classes '*UserDBManager*' and '*StatsDBManager*', the **username** and **password** attributes are your mysql details, 
  otherwise you won't be able to access the databases from the program.
There also maybe an issue with the *mysql-connector.jar* (in the lib directory), so remember to add that to the classpath


#### Game
Currently, to start a not functioning game between 2 users, use the command
	'game start $User 1 $User 2'
	
If the 2 users are online, then a game will start.


### Server System

The system I have implemented to communicate between machines is a **UDP Socket** system.
The Host and Each Client receives and sends DatagramPackets which contain a serialized **Packet** object.
When a client logins/signups up, it sends a packet to the host, which authorizes and registers the client's address, port and other information
  internally and sends packets to the client using the address and port.
  
Both the Host and Clients handle a received packet in their *main* class with a handle method,
  which uses a switch statement on the packet's header (*custom enum*) to decide the packet's use.

