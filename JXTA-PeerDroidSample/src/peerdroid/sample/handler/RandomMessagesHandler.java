package peerdroid.sample.handler;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import peerdroid.sample.neighbours.Peer;
import peerdroid.sample.service.JXTAService;
import peerdroid.sample.service.SocketService;


/**
 *  P2P Streaming Player for Google Android
 *  
 *  Connection Handler used to send and receive message over socket.
 *
 * @author   Marco Picone 
 * @created  01/09/2008
 */
public class RandomMessagesHandler implements Runnable {
	
	private String TAG = "RANDOM MESSAGES HANDLER ";
	private ArrayList<Peer> peerList = null;
	private SocketService socketService = null;
	private JXTAService jxtaManager = null;
	
    /**
     * Class constructor
     * @param socket
     */
    public RandomMessagesHandler(JXTAService jxtaManager) {
    	this.jxtaManager = jxtaManager;
    }

    /**
     * Thread run method
     */
    public void run() {
    
    	while(true)
    	{	
    		this.socketService = this.jxtaManager.getSocketService();
    		this.peerList = this.jxtaManager.getPeerList();
    		
    		if(peerList.size() > 0)
    		{
    			try
    			{
    				int randomIndex = (int)((100.0*Math.random())%peerList.size());
        			
        			System.out.println(TAG+"Sending Message to " + peerList.get(randomIndex).getName() + " Random:" + randomIndex);
            		
            		if(this.socketService.connectToSocket(peerList.get(randomIndex).getPipeAdvertisement()) != -1)
            		{
            			//If there is no response ... remove peer from List
            			if(this.socketService.sendMessage(jxtaManager.getInstanceName() + ":Hello " + peerList.get(randomIndex).getName() + " !") == -1)
            			{
            				System.err.println(TAG+"ERROR SENDING MESSAGE TO " + peerList.get(randomIndex).getName());
            			}
            			
            			this.socketService.closeConnection();
            		}
            		else
            		{
        				System.err.println(TAG+"ERROR CONNECTING TO " + peerList.get(randomIndex).getName());
        				peerList.remove(randomIndex);
            		}
    			}
    			catch(Exception e)
    			{
    				System.err.println(TAG+e.getLocalizedMessage());
    			}
    			
    		}
    	    		
    		try {
				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }
}
