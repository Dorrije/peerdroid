package peerdroid.sample.main;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import peerdroid.sample.handler.RandomMessagesHandler;
import peerdroid.sample.handler.SocketServerHandler;
import peerdroid.sample.service.JXTAService;

public class Main {

	private static String myPeerName;
	private static JXTAService manager;

	public static void main(String args[]) {

		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Insert PeerName: ");
		try {
			myPeerName = stdin.readLine();
		} catch (IOException e1) {
			System.exit(0);
		}


		System.out.println("Sono il Peer: " + myPeerName );

		System.setProperty("net.jxta.logging.Logging", "OFF");
		manager = new JXTAService(myPeerName,"principal","password");

		//Creo il Thread per la gestione della ServerSocket di questo peer
		Thread serverThread = new Thread(new SocketServerHandler(manager.getSocketService()), "Connection Handler Thread");
		serverThread.start();
		
		manager.searchPeerName("Prova");
		
		Thread randomMessagesThread = new Thread(new RandomMessagesHandler(manager));
		randomMessagesThread.start();
	}
	
}
