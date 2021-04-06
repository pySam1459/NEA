# Executable Jars

To use one of the jar files, copy/cut and past the jar file into the parent directory (with src, lib, res, ...)
Then run the executable, either by double clicking, or running
	```bash
	java -jar X.jar
	```
	
where X.jar is the jar file


# Host

If the Host.jar file is available, a few things need to be set up:

### mysql
```sql
CREATE DATABASE OnlinePoolGame;

CREATE USER 'admin'@'localhost' IDENTIFIED BY '$password';
GRANT ALL PRIVILEGES ON * . * TO 'admin'@'localhost';
FLUSH PRIVILEGES;
```

'$password' can be up to you.
A file 'res/misc/sqlCredentials.cred' is to be made containing:
```text
admin
$password
```
Otherwise, "Error loading SQL Database Credentials" will be displayed.

### firewall
Host.jar uses the port 5303.
For linux, this command should be run:
```bash
ufw allow 5303
```

For other OSs, 5303 should be open.

### running Host.jar

Host.jar should be run in the console/shell.
Once you have executed Host.jar, a login should appear in the console.
The login credentials are 'admin', 'password'

For the first time running, 2 commands should be run:
```bash
db create users
db create stats
```

Then, to start the server:
```bash
server start
```

And the Host should be working!