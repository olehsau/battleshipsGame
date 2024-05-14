The application connects to another application and plays Battleships game.

### Running parameters
The application handles the following parameters:
* `-mode [server|client]` - indicates the mode of operation (as server: accepts a connection, as client: establishes a connection with the server).
* `-host N` - only in client mode: the IP of the server with which the application will communicate. Example: `-host 192.168.2.12`.
* `-port N` - the port on which the application is to communicate. Example: `-port 4567`.
* `-map map-file` - the path to a file containing a map with ship deployment (format described in the Map section).

Communication is done using the TCP protocol, with UTF-8 encoding.
The client and server alternately send each other messages, which consist of 2 parts: command and coordinates, separated by `;`, and ending with the end-of-line character ``n`.
Message format: `command;coordinates`.
