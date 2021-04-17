# Executable Jars

To use one of the jar files, copy/cut and paste the jar file into the parent directory (with src, lib, res, ...)
Then run the executable, either by double clicking, or running
	```
	java -jar X.jar
	```
	
where X.jar is the jar file.


# Host

To run the Host program, a few things need to be set up:

### mysql
A mysql database called 'OnlinePoolGame' should be created and an admin user with all privileges should be added.
The file 'scripts/mysqlSetup.sql' (shown below), will automatically do this, but the password in the script should suitably be changed.

```sql
CREATE DATABASE OnlinePoolGame;

CREATE USER 'admin'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON * . * TO 'admin'@'localhost';
FLUSH PRIVILEGES;
```

The credentials of this admin user should be placed in a file 'res/misc/sqlCredentials.cred'
```
admin
password
```

Otherwise, "Error loading SQL Database Credentials" will be displayed.
**NOTE: if the password is left as 'password', mysql will not allow the user to be made as password requirements must be met.**

### firewall
Host.jar uses the port 5303.
For linux, this command should be run:
```
ufw allow 5303
```

For other OSs, make sure 5303 is open.

### running Host.jar

Host.jar should be run in the console/shell.
Once you have executed Host.jar, a login should appear in the console.
The login credentials are 'admin', 'password'

For the first time running, this command should be run:
```
db setup
```

Then, to start the server:
```
server start
```

And the Host should be running!


# Client

To run the Client program, place it in the parent directory and run the executable jar file.
A GUI should appear, giving you a prompt to login / sign up. If you do not already have an account, sign up by inputting the required information and press sign up.

