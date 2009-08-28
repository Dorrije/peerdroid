package peerdroid.sample.handler;

import peerdroid.sample.PeerDroidSample;
import peerdroid.sample.service.SocketService;
import android.util.Log;

/**
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
		
		Log.d(PeerDroidSample.TAG, TAG + "New Thread to manage SocketServer !!");
	}
	
	/**
     * Thread run method
    */
	public void run() {
		
		//Creates JXTA ServerSocket
		local_socketService.createSocket();
	}
	
}
