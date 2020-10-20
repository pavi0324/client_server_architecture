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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;

public class ClientDS extends JFrame  {
	
	private static final long serialVersionUID = 1L;
	JFrame frame;
	JLabel userName;
	JTextField uNameField;
	JButton sendUName, quit, dont_wait;
	JTextArea display;
	JScrollPane scroll;
	JLabel label1;
	JTextArea label2;
	Timer timer;
	int count = 0;
	boolean connection = true, wait = true;
	
	public ClientDS() {
		createClientGUI();
	}

	// Creating a Client GUI
	public void createClientGUI() {
		// create a frame and Panel for Client GUI
		frame = new JFrame("Client GUI");
		JPanel jp = new JPanel();

		// User Name Label
		userName = new JLabel("UserName:");
		jp.add(userName);

		// Text Field to Type User Name
		uNameField = new JTextField(30);
		//uNameField.setBounds(80, 30, 120, 40);
		jp.add(uNameField);

		// Send Button to connect to server with typed user name
		sendUName = new JButton("Send");
		jp.add(sendUName);

		// Text Area to display messages
		display = new JTextArea(10, 50);
		display.setEditable(false);
		display.setLineWrap(true);
		scroll = new JScrollPane(display);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jp.add(scroll);

		// Timer to display the thread pausing time 
		label1 = new JLabel("Timer for Pause command");
		label2 = new JTextArea(1,5);
		jp.add(label1);
		jp.add(label2);
		
		// quit button to close the client 
		quit = new JButton("QUIT");
		jp.add(quit);
		
		//Cancel Button to skip waiting for server pause message
		dont_wait = new JButton("CANCEL");
		dont_wait.setEnabled(false);
		jp.add(dont_wait);

		frame.add(jp);
		frame.setSize(700, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
	}
	
	// function that interrupts the paused thread when cancel button is pressed
	public void cancelWaiting(final DataOutputStream dos) {
		dont_wait.addActionListener(new ActionListener() {
			Thread main = Thread.currentThread();
			public void actionPerformed(ActionEvent ae) {	
				timer.stop();	                // Stop the timer display			
				try {
					main.interrupt();           // Interrupt the sleeping thread
					dos.writeUTF("IGNORED");    // Send reply to Server that thread stopped waiting
					wait = false;               // set wait to false
				} catch (IOException e) {
					e.printStackTrace();
				}
				display.append("User Stopped Waiting \n"); // Display the message that it stopped waiting in Client GUI		
			}
		});
	}


	public static void main(String[] args) {
		/*Reference 
		  * 1) https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/
		  */
		 try 
	        { 	
			 	// getting localhost ip 
	            InetAddress ip = InetAddress.getByName("localhost");	            
	            // establish the connection with server port 5056 
	            final Socket s = new Socket(ip, 5056);
	            // Create a client GUI
	            final ClientDS clientGUI = new ClientDS();
	            
	            // Get Input and output streams for the client socket
	            final DataInputStream dis = new DataInputStream(s.getInputStream()); 
	            final DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
	            
	            // send Username to server when user clicks Send button in client GUI
	            clientGUI.sendUName.addActionListener(new ActionListener() {
	            	public void actionPerformed(ActionEvent ae) {
	            		try {
	            			String userName = clientGUI.uNameField.getText(); // Get the text(userName) entered by User from GUI
	            			dos.writeUTF(userName); // Send the username to client
						} catch (IOException e) {
							e.printStackTrace();
						}
	            	}
	            });
	            
	            // close the connection and Client GUI when the "QUIT" button is pressed
	            clientGUI.quit.addActionListener(new ActionListener() {
	            	public void actionPerformed(ActionEvent ae) {
	            		try {
	            			clientGUI.connection = false; // Set connection to false so when the quit button is pressed, 
	            										  // the client stops waiting for server command and close socket and IO streams conecctions
	            										  // Dispose frame 
	            			dos.close();
							dis.close();
							s.close();
	            			clientGUI.frame.dispose();
							
						} catch (IOException e) {
							e.printStackTrace();
						}
	            	}
	            });
	            
	            // Skip pausing if the "CANCEL" is pressed by the user
	            clientGUI.cancelWaiting(dos);
	            
	            /* Receive if the user name is available to connect.
	             * 1) "NOT AVAILABLE" - Client is disconnected and input and output streams are closed.
	             * 2) "DONE" - User Name Available and connected to client. Wait for Server 's Pause Command
	             */
	            String received = dis.readUTF(); // Read input from Server
	            String random;	// To store random number received from server   
	            // quit and close everything when the user name is not available
	            if(received.equals("NOT AVAILABLE")) {
	            	String dispText = "UserName Conflict. Quitting Session. Connect with New User name\n";
	            	clientGUI.display.append(dispText + "\n");
	            	Thread.sleep(2000);
	            	dis.close();
	                dos.close();
	                s.close();
	                clientGUI.frame.dispose();
	            }
	            // Once the User name is available wait for Pause command and Random number
	            else if(received.equals("AVAILABLE")) {
	            	String dispText = "Successfully Connected to the Server \n";
	            	clientGUI.display.append(dispText + "\n");		            	
	            	clientGUI.display.append("Waiting for Server Random number: \n ");
	            	while(clientGUI.connection) {	            			            		
	            		try {
	            			random = dis.readUTF();	 // Read "PAUSE" command           									
	            		}catch (IOException e) {
							continue;
						}
	            		if(random.equals("PAUSE")) {
	            			clientGUI.display.append("Pause Command Received: \n");
	            			clientGUI.wait= true; // set client to wait state by default. 
	            			random = dis.readUTF();	 // Read Random Number Sent by user
	            			clientGUI.count = Integer.parseInt(random); // Convert String received by client it to Integer number
		            		int time = clientGUI.count * 1000;	 // Multiply the random number sent by 1000 to change the number to milliseconds to add to Thread.sleep(milliseconds)		            		
		            		/**********************************************************************/
		            		/*  Timer to display the countdown in GUI                    		            		
		            		 *  Reference -https://stackoverflow.com/questions/22728794/how-to-put-timer-into-a-gui 
		            		 *  Get the random number from client and decrement it by 1 and display it in GUI.
		            		 */
							clientGUI.dont_wait.setEnabled(true);					
							    clientGUI.timer = new Timer(1000, new ActionListener() {							  
							    @Override 
								public void actionPerformed(ActionEvent e) {					    
								  clientGUI.count--; 
								  if(clientGUI.count > -1) {
									  clientGUI.label2.setText(Integer.toString(clientGUI.count)); // Set the counter number to the GUI each Second					  
								  } else { 
									  ((Timer) (e.getSource())).stop(); 
								  } } 
							});
							clientGUI.timer.setInitialDelay(0); 
							clientGUI.timer.start();	// Start the timer and pause the thread
							clientGUI.display.append("User Waiting.. \n"); // Display in GUI that user is waiting for the given number in seconds by server
							/**********************************************************************/
							try {
								// Pause the thread for the time(random number) sent by the Server
								Thread.sleep(time);
							} catch (InterruptedException e1) {	
								// When pause is interrupted,
								// * set wait state to false which will not send "DONE" command to Server
								// * continue waiting for the pause command from the server
							    clientGUI.wait = false;								  
							    continue;						
							}
							
							if(clientGUI.wait) {
								// Send "DONE" to server when the thread is done with pausing and display it in GUI
								dos.writeUTF("DONE");
								dos.writeUTF(random);
								clientGUI.display.append("User is done Waiting. Listening to further Commands \n");
							}
	            		}
	            	}	            	
	            }
	            dis.close();
                dos.close();
                s.close();
	        }catch(Exception e){ 
	            e.printStackTrace(); 
	        }
	}
}