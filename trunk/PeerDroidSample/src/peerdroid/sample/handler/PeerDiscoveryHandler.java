package peerdroid.sample.handler;

import peerdroid.sample.PeerDroidSample;
import peerdroid.sample.service.JXTAService;
import android.util.Log;
import net.jxta.discovery.DiscoveryService;

/**
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
		
		Log.d(PeerDroidSample.TAG,"New Thread to manage peers research !!");
	}
	
    /**
     * Thread run method
     */
	public void run() {
		
		//Starts peer discovery
		try {
	            
				while ( !this.manager.isStopped() ) {
	            
					 // Add ourselves as a DiscoveryListener for DiscoveryResponse events
					manager.getDiscovery().addDiscoveryListener(manager);
					
	            	manager.getDiscovery().getRemoteAdvertisements(null, DiscoveryService.ADV,null, null, 200,null);
	            	
	            	//looks for socket peer
	            	//manager.getDiscovery().getRemoteAdvertisements(null, DiscoveryService.PEER,"Name", peerName, 1,null);
	            	
	            	//looks for Android peer
	            	//manager.getDiscovery().getRemoteAdvertisements(null, DiscoveryService.PEER,"Name", "jxmeAndroid", 1,null);
	            	
	                //looks for any peer
	            	//manager.getDiscovery().getRemoteAdvertisements(null, DiscoveryService.PEER,null, null, 20);
	            	
	                //wait a bit before sending next discovery message
	                try{	
	                	Thread.sleep(60 * 1000);     		
	                } catch(Exception e) {}
					
	            } //end while
				
				Log.d(PeerDroidSample.TAG,"Closing PeerDiscoveryHandler ...");
	            
	        } catch(Exception e) {
	            e.printStackTrace();
	        }
		
	}
	
}