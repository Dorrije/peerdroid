package it.p2p.handler;

import android.util.Log;
import it.p2p.P2PStreaming;
import it.p2p.service.SocketService;

/**
 *  P2P Streaming Player for Google Android
 *  
 *  Handler used to create JXTA ServerSocket.
 *
 * @author   Marco Picone 
 * @created  01/09/2008
 */
public class SocketServerHandler implements Runnable{

	//Object used to manage the JXTA Socket Service
	private SocketService local_socketService;
	
	private String TAG = "SOCKET SERVER HANDLER ";
	
	public SocketServerHandler(SocketService socketService){
		
		this.local_socketService = socketService;
		
		Log.d(P2PStreaming.TAG, TAG + "Nuovo Thread per la gestione della SocketServer !!");
	}
	
	/**
     * Thread run method
    */
	public void run() {
		
		//Creates JXTA ServerSocket
		local_socketService.createSocket();
	}
	
}
