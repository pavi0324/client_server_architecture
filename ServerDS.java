/*
 * Student Name : Pavithra Rathinasabapathy
 * Student ID: 1001698736 
 * 
 * References:
 * Below links are used to develop this project
 * https://stackoverflow.com/questions/15247752/gui-client-server-in-java 
 * https://stackoverflow.com/questions/22728794/how-to-put-timer-into-a-gui
 * https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
 * https://www.geeksforgeeks.org/socket-programming-in-java/
 * https://stackoverflow.com/questions/41506997/java-multithreading-synchronizedthis-on-a-thread-class
 */

package project1DS;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;


public class ServerDS {
	
	private static ServerSocket ss = null;
	private static Socket s  = null;
	private static final int maxClientsCount = 3; // variable to store maximum clients - in this case 3
	private static final ClientHandler[] threads = new ClientHandler[maxClientsCount]; // thread array to store all the threads
	private static final HashMap<String,Socket> clientList = new HashMap<String,Socket>(); // cleint list to store all client name and check for user name conflicts
	
	
	
	public static void main(String[] args) throws IOException {
		
		// open a server socket 
		ss = new ServerSocket(5056);
		// Create a Server GUI
		final ServerGUI gui = new ServerGUI();
		//Display that Server is waiting for Connections
		gui.display.append("Welcome! Waiting for the Clients to connect.. \n");
		
		// Create a separate thread that selects a random client and the generate a random number and sends 
		// to particular client
		final ServerThread randomGenerator = new ServerThread(gui);
		Thread rG = new Thread(randomGenerator);
		rG.start(); // Start thread
		
	
		while(true) {
			s = null;			
			try {
				s=ss.accept();	 // Accept Client Connections
				// open input and output streams
				DataInputStream dis = new DataInputStream(s.getInputStream());
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				
				/* 
				 * This block of code gets the user name from the Client and checks if already another client
				 * with the same user name is present. 
				 * If yes, it sends a "NOT AVAILABLE" message to the client 
				 * and closes the connection to the client.
				 * If No, then adds the current client's user name to clientList and creates a new thread for the client
				 * and adds the thread to thread array.
				 *   
				 */
				String uName = dis.readUTF();
				if(!clientList.containsKey(uName)) {					
					int i=0;
					for(i=0;i<maxClientsCount; i++) {
						dos.writeUTF("AVAILABLE");         // Send message indicating username available
						if(threads[i] == null) {														
							clientList.put(uName,s); // add current username to the clientlist 
							threads[i] = new ClientHandler(s, gui, dis,dos, uName,threads); // create a new thread 
							randomGenerator.list.add(threads[i]); // add this thread to the list of client Hander for the server 
																  // thread to pick a random client
							randomGenerator.currentClientCount += 1; //increment client count. ServerThread starts picking random client only when there is more than 0 clients.
																	 // incrementing this would start the ServerThread to randomly pick the client
							Thread t = new Thread(threads[i]);
							t.start();							
							break;
						}
					}
					// Allowing only 3 clients to connect. when there are more than 3 clients, reject the connection
					if(i == maxClientsCount) {
						dos.writeUTF("Max Client Count Reached.. Server Busy.. Try again Later");
						dis.close();
						dos.close();
						s.close();					
					}
				}
				else {
					dos.writeUTF("NOT AVAILABLE"); // if username is not availble, send message and reject the connection
					dos.close();
					dis.close();
					s.close();
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}	
		}	
	}
}

class ServerThread implements Runnable{

    int currentClientCount;
    public List<ClientHandler> list;
    public ServerGUI gui;

    
    public ServerThread(ServerGUI gui) {
    	list = new ArrayList<ClientHandler>();
    	this.gui = gui;
    }
    /*  
     * this function selects a random client from the given client list
     */
    public ClientHandler getRandomElement(List<ClientHandler> list) 
    { 
        Random rand = new Random();  
        return list.get(rand.nextInt(list.size())); 
    } 
      
    /*  
     * this function selects a random number from 3 to 9
     */
    private static int getRandomNumberInRange(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
    
    /*
     * this code waits until atleast one client is connected and then starts randomly
     * picking up the client, it generates a random number and sends it to that Client.
     * This server thread selects a random client and send number for every 10 seconds  
     */
    public void run() {
		
    	// wait until when at least one client is connected.
		while(currentClientCount == 0) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			continue;
		}
		
		// when there is one or more client, pick them randomly and send a ramdom number 
		while(currentClientCount != 0) {			
			ClientHandler result = getRandomElement(list);	 // call to get a random client		
			int randomNumber = getRandomNumberInRange(3,9);  // call to get a random number
			try {
					gui.display.append("PAUSE command sent to " + result.s +"\n");
					result.dos.writeUTF("PAUSE");           // Send PAUSE command to randomly picked Client
					result.dos.writeUTF(Integer.toString(randomNumber)); // send the random number
					Thread.sleep(10000); // wait for 10 seconds before randomly selecting next client										
			} catch (IOException e) {
				// if the client quits, remove them from the client list and decrement the client count and display it in server GUI
				currentClientCount--;
				list.remove(result);
				gui.display.append("Pausing process stopeed for User: " + result.s);
				continue;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
    }	
}


/* ClientHandler Thread - Creates a separate thread for each client
 * This block keeps reading the messages form the Client and Displays them in the ServerGUI.
 * Display all the connected user to the Server GUI
 */ 
class ClientHandler implements Runnable {
	
	public ClientHandler[] threads; // Array of Threads to 
    public Socket s;                // Socket of the current Thread
    public ServerGUI gui;           // Server GUI object to display the message from client
    public DataInputStream dis;     // Input Stream of Current Thread
    public DataOutputStream dos;	// Output Stream of Current Thread 
    public String name;				// To Store the User name of current thread
    public int maxClients;			// Max Client to loop through threads
    
    
    public ClientHandler(Socket s, ServerGUI gui, DataInputStream dis, DataOutputStream dos, String uName, ClientHandler[] threads) throws IOException
    { 
        this.s = s; 
        this.gui = gui;
        this.dis = dis;
        this.dos = dos;
        this.name = uName; 
        this.threads = threads;
        this.maxClients = threads.length;
    } 
    
	public void run() {		
		String uName = this.name;
		
		
		/* This block of Code displays that current Thread/User have joined the server
		 * and displays all the currently connected users in its GUI
		 * Loop through the Threads and display their names
		 */
		synchronized(this) {
			gui.display.append("User " + uName +  " Joined \n");
			gui.display.append("Users Currently Connected: \n");
			for(int i =0;i<this.maxClients;i++) {
				if(threads[i] != null) {
					gui.display.append(threads[i].name + "\n");
				}
				
			}
		}
		
		/*
		 * This block keeps reading from the Client/User for the Commands such as 
		 *  "DONE" - The user has done waiting for the given random number in seconds and replied positive
		 *  "IGNORED" - The user has stopped or ignored waiting
		 *  "QUIT" - The user is quitting the connection - Thread killed by the user
		 */
		
		while(true) {
			try {
				String reply = dis.readUTF(); // read the incoming message from the user
				if(reply.equals("DONE")){ // if done, display that client has done waiting
					String time = dis.readUTF();
					gui.display.append("Client "+ uName + " waited "+ time +" seconds for Server \n"); 
				}
				else if(reply.equals("IGNORED")) { // if client cancelled waiting, display it to the GUI
					gui.display.append("User "+ uName + " ignored waiting \n");
				}
				else if(reply.equals("QUIT")) { //if Client "QUITS"
					// Close all the connections and end waiting for messages from client
					s.close();
					dos.close();
					dis.close();
					break;
				}
				
			} catch (IOException e) {
				try {
					// Once the user gives "QUIT" command, remove the current thread from the array of thread
					synchronized(this) {
						for(int i =0;i<this.maxClients;i++) {
							if(threads[i] != null) {
								if(threads[i].name.equals(uName)) { // check for current thread using name
									threads[i] = null;
								}
							}							
						}
					}				
					// Display that the user has left to Server GUI and close connections
					gui.display.append("User: "+ uName + " left \n"); 
					s.close();
					dos.close();
					dis.close();
					break;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}		
	}
}


class ServerGUI extends JFrame {
		
	private static final long serialVersionUID = 1L;

	JFrame frame;
	JTextArea display;
	JScrollPane scroll;
	
	public ServerGUI() {
		
		// Create a frame and Jpanel to the ServerGUI
		frame = new JFrame("Server GUI");
		JPanel jp = new JPanel();
		
		// Text Area to display the messages
		display = new JTextArea(20,50);
		display.setEditable(false);
		
		//Scroll pane to scroll the text area
		scroll = new JScrollPane(display);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jp.add(scroll);
		
		frame.add(jp);
		frame.setSize(700, 600); //set Dimension of the Frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close on clicking X button
		frame.setVisible(true); 
		
	}
}
