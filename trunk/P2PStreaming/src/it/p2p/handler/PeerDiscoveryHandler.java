package it.p2p.handler;

import android.util.Log;
import it.p2p.P2PStreaming;
import it.p2p.service.JXTAService;
import net.jxta.discovery.DiscoveryService;

/**
 *  P2P Streaming Player for Google Android
 *  
 *  Handler used to discovery peers in JXTA Network.
 *
 * @author   Marco Picone 
 * @created  01/09/2008
 */
public class PeerDiscoveryHandler implements Runnable{

	//Manager of JXTA Service
	private JXTAService manager;
	
	//JXTA Perr Name
	private String peerName;
	
	private String TAG = "PEER DISCOVERY HANDLER ";
	
	/**
	 * Class constructor
	 * @param manager
	 * @param peerName
	 */
	public PeerDiscoveryHandler(JXTAService manager, String peerName){
		
		this.manager = manager;
		this.peerName = peerName;
		
		Log.d(P2PStreaming.TAG,"Nuovo Thread per la gestione della ricerca di un Peer !!");
	}
	
    /**
     * Thread run method
     */
	public void run() {
		
		//Starts peer discovery
		try {
	           // Add ourselves as a DiscoveryListener for DiscoveryResponse events
				manager.getDiscovery().addDiscoveryListener(manager);
	            
				while ( true ) {
	            
					Log.d(P2PStreaming.TAG, TAG + "Sending peer discovery message for : " + peerName );
	            	
	            	//looks for socket peer
	            	manager.getDiscovery().getRemoteAdvertisements(null, DiscoveryService.PEER,"Name", peerName, 1,null);
	            	
	            	//looks for Android peer
	            	manager.getDiscovery().getRemoteAdvertisements(null, DiscoveryService.PEER,"Name", "jxmeAndroid", 1,null);
	            	
	                //looks for any peer
	            	manager.getDiscovery().getRemoteAdvertisements(null, DiscoveryService.PEER,null, null, 20);
	            	
	                //wait a bit before sending next discovery message
	                try{	
	                	Thread.sleep(60 * 1000);     		
	                } catch(Exception e) {}
					
	            } //end while
	            
	        } catch(Exception e) {
	            e.printStackTrace();
	        }
		
	}
	
}