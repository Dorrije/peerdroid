package peerdroid.sample.handler;


import java.util.ArrayList;

import android.util.Log;

import peerdroid.sample.PeerDroidSample;
import peerdroid.sample.neighbours.Peer;
import peerdroid.sample.service.JXTAService;
import peerdroid.sample.service.SocketService;


/**
 *  Random Message Handler sends random message to available
 *  peers.
 *  
 *
 * @author   Marco Picone  picone.m@gmail.com
 * @created  27/08/2009
 */
public class RandomMessagesHandler implements Runnable {
	
	private String TAG = "RANDOM MESSAGES HANDLER ";
	private ArrayList<Peer> peerList = null;
	private SocketService socketService = null;
	private JXTAService jxtaManager = null;
	private boolean stopThread;
	
    /**
     * Class constructor
     * @param jxtaManager
     */
    public RandomMessagesHandler(JXTAService jxtaManager) {
    	this.jxtaManager = jxtaManager;
    }

    /**
     * Thread run method
     */
    public void run() {
    
    	Log.d(PeerDroidSample.TAG,TAG+"RandomMessagesHandler");
    	
    	while(PeerDroidSample.programClosed == false)
    	{	
    		this.socketService = this.jxtaManager.getSocketService();
    		this.peerList = this.jxtaManager.getPeerList();
    		
    		if(peerList.size() > 0)
    		{
    			try
    			{
    				int randomIndex = (int)((100.0*Math.random())%peerList.size());
        			
        			Log.d(PeerDroidSample.TAG,TAG+"Sending Message to " + peerList.get(randomIndex).getName() + " Random:" + randomIndex);
            		
            		if(this.socketService.connectToSocket(peerList.get(randomIndex).getPipeAdvertisement()) != -1)
            		{
            			//If there is no response ... remove peer from List
            			if(this.socketService.sendMessage(jxtaManager.getInstanceName()+": " + PeerDroidSample.outgoingMessage) == -1)
            			{
            				Log.d(PeerDroidSample.TAG,TAG+"ERROR SENDING MESSAGE TO " + peerList.get(randomIndex).getName());
            			}
            			
            			this.socketService.closeConnection();
            		}
            		else
            		{
            			Log.d(PeerDroidSample.TAG,"ERROR CONNECTING TO " + peerList.get(randomIndex).getName());
        				peerList.remove(randomIndex);
            		}
    			}
    			catch(Exception e)
    			{
    				Log.d(PeerDroidSample.TAG,TAG+e.getLocalizedMessage());
    			}
    			
    		}
    	    		
    		try {
				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	
    	Log.d(PeerDroidSample.TAG,TAG+"RandomMessage Handler Closed !");
    }
 
}
