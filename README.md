# client_server_architecture

This project consist of system consisting of a server and three client processes. Each client process will connect to the server over a socket connection and 
register a username at the server. The server should be able to handle all three clients simultaneously and display the names of the connected clients in real time.
Two or more clients may not use the same username simultaneously. Should the server detect a concurrent conflict in username, the client’s connection should be rejected, 
and the client’s user should be prompted to input a different name. Every ten seconds, the server will randomly select a connected client and send that client an 
integer between 3 and 9. Upon receiving the integer, the client will pause (e.g., sleep or otherwise suspend) the thread managing the connection to the server for a 
period equaling the value received from the server, in seconds. The client’s GUI will maintain a decrementing countdown timer indicating when the thread will resume, 
as well as a button to skip the wait and resume the thread’s operation immediately. When the client thread is finished waiting, it will reply to the server with a message 
stating, “Client <name> waited <#> seconds for server.” The server will display this message on its GUI. This sequence will be repeated until the components are 
manually terminated by the user.

Project IDE: Eclipse IDE for Java Developers - 2019-12
Steps for setting up a project: 
-	Create a new java project in Eclipse IDE
-	Create a new package under src folder with the name “project1DS”
-	Copy the files ClientDS.java and ServerDS.java to the project1DS folder
-	Run the ServerDS.java as java Application
-	Run the ClientDS.java as java application and enter the name to get a connection
-	Similarly run this file for next 2 clients in the same way.
-	The randomly selected client would receive random number and thread is paused.
-	The timer Label in GUI shows the countdown
-	Press the “CANCEL” button on the Client GUI to stop waiting
-	Press the “QUIT button to manually kill the client.
-	The text Area in Client GUI displays all the messages from Server.
-	The text area in Server GUI displays all the messages from Client.
Message sent from Client to Server:
1)	“DONE” – client thread is finished with waiting
2)	“IGNORED” – client thread cancelled waiting. Didn’t wait
3)	“QUIT” – User manually killed the client. Close all the connection
Messages from Server to Client:
1)	“AVAILABLE” – The user name is available and connection is made to server
2)	“NOT AVAILABLE” – The user name is not available and clients rejects the connection. The client GUI is disposed and have to run the client again
3)	“PAUSE” - Pause command from server. Expect a random number.

Assumption - Server selects client randomly every 10 seconds. Even if the selected clients didn’t pause/wait or wait for less than 10 seconds (e.g., 3 sec). 
Upon receiving the reply from client, server will wait for that remaining seconds to randomly select next client.

Project Reference- 
1.	https://stackoverflow.com/questions/15247752/gui-client-server-in-java 
2.	https://stackoverflow.com/questions/22728794/how-to-put-timer-into-a-gui
3.	https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
4.	https://www.geeksforgeeks.org/socket-programming-in-java/
5.	https://stackoverflow.com/questions/41506997/java-multithreading-synchronizedthis-on-a-thread-class
