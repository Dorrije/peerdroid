package peerdroid.sample.service;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.security.cert.CertificateException;

import peerdroid.sample.PeerDroidSample;
import peerdroid.sample.dialog.IntroDialog;
import peerdroid.sample.handler.PeerDiscoveryHandler;
import peerdroid.sample.neighbours.Peer;

import android.util.Log;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.impl.config.Config;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.ConfigurationFactory;
import net.jxta.platform.ModuleClassID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.rendezvous.RendezVousService;

/**
 *  JXTA Service based on peerdroid
 * 
 *  Class used to manage all service linked with the JXTA System
 *
 * @author     Marco Picone 
 * @created    01/09/2008
 */

public class JXTAService implements DiscoveryListener {

	private DiscoveryService discovery;
	private PeerGroup netPeerGroup = null;
	
	private RendezVousService rendezvous;
	private String instanceName = "NA";
	
	private static String rdvlist = "http://www.ce.unipr.it/~picone/rdvlist.txt";
	//private static String rdvlist = "http://dsg.ce.unipr.it/research/SP2A/rdvlist.txt";
	
	private PeerID socketPeerID;
	private PeerID rdvId;
	
	private SocketService socketService;
	private NetworkManager networkManager;
	private NetworkConfigurator config;
	private Thread discoveryThread, serverThread;
	private String principal, password;
	private File instanceHome;
	
	private boolean stopped = false;
	private boolean founded = false;
	
	public ArrayList<Peer> peerList = new ArrayList<Peer>();
	private boolean connected = false;

	public File getInstanceHome() {
		return instanceHome;
	}

	public void setInstanceHome(File instanceHome) {
		this.instanceHome = instanceHome;
	}

	public ArrayList<Peer> getPeerList() {
		return peerList;
	}

	public void setPeerList(ArrayList<Peer> peerList) {
		this.peerList = peerList;
	}

	/**
	 * Class constructor, sets variables and starts JXTA Platform 
	 * @param instanceName
	 * @param principal
	 * @param password
	 * @param instanceHome
	 * @param socketPeerName
	 */
	public JXTAService(String instanceName, String principal, String password,File instanceHome) {

		Log.d(PeerDroidSample.TAG, "JXTAService Costructor !");

		this.instanceName = instanceName;
		this.principal = principal;
		this.password = password;
		this.instanceHome = instanceHome;

		start(principal, password);
	}

	/**
	 *  Creates and starts the JXTA NetPeerGroup using a platform configuration
	 *  template. This class also registers a listener for rendezvous events
	 *
	 * @param  principal  principal used the generate the self signed peer root cert
	 * @param  password   the root cert password
	 */
	public void start(String principal, String password) {
	
		try {

			//Creating the NetworkManager
			networkManager = new NetworkManager(NetworkManager.EDGE,
					"My Local Network", instanceHome.toURI());

			//Persisting it to make sure the Peer ID is not re-created each time 
			// the NetworkManager is instantiated
			networkManager.setConfigPersistent(true);

			//Retrieving the Network Configurator
			config = networkManager.getConfigurator();

			config.setHome(instanceHome);
			
			/*
			if (!config.exists()) {

				config.setPeerID(IDFactory
						.newPeerID(PeerGroupID.defaultNetPeerGroupID));
				//config.setPeerID(JXTASystem.getMY_ID());
				Log.d(PeerDroidSample.TAG, "PeerID Impostato: "
						+ JXTASystem.getMY_ID());
				config.setName(instanceName);
				config.setDescription("Created by AndroidTester");
				config.setMode(NetworkConfigurator.EDGE_NODE);
				config.setPrincipal(principal);
				config.setPassword(password);
				config.setUseMulticast(false);
				try {
					config.addRdvSeedingURI(new URI(rdvlist));
				} catch (java.net.URISyntaxException use) {
					use.printStackTrace();
				}
				try {
					config.save();
				} catch (IOException io) {
					io.printStackTrace();
				}
			} else {

				try {
					Log.d(PeerDroidSample.TAG, "Loading found configuration");
					File LocalConfig = new File(config.getHome(),
							"PlatformConfig");
					config.load(LocalConfig.toURI());
					Log.d(PeerDroidSample.TAG, "Configuration loaded");
				} catch (IOException ex) {
					Log.d(PeerDroidSample.TAG,
							"Configuration Loading Exception !!!");
					ex.printStackTrace();
					System.exit(-1);
				} catch (CertificateException e) {
					e.printStackTrace();
				}
			}
*/
			//ConfigurationFactory.setRelayed(true);
			ConfigurationFactory.setName(instanceName);
			ConfigurationFactory.setTCPPortRange(9700, 9799);
			//ConfigurationFactory.setTCPPortRange(9700, 9702);
			ConfigurationFactory.setPeerID(config.getPeerID());
			ConfigurationFactory.setRdvSeedingURI(new URI(rdvlist));	
			
			ConfigurationFactory factory = ConfigurationFactory.newInstance();
			factory.setUseOnlyRelaySeeds(true);
			factory.setUseOnlyRendezvousSeeds(true);
		
			Log.d(PeerDroidSample.TAG, "RDV: " +  config.getStoreHome().toString());
			Log.d(PeerDroidSample.TAG, "RDV: " +  config.getName());
			Log.d(PeerDroidSample.TAG, "RDV: " +  config.getPeerID());
			
			netPeerGroup = networkManager.startNetwork();

			Log.d(PeerDroidSample.TAG, "NET PEER GROUP: " +  netPeerGroup.getPeerGroupID());
			Log.d(PeerDroidSample.TAG, "NET PEER NAME: " +  netPeerGroup.getPeerGroupName());
			
			rendezvous = netPeerGroup.getRendezVousService();
			discovery = netPeerGroup.getDiscoveryService();

			//Creates SocketService from NetPeerGroup
			socketService = new SocketService(this);

			//Publish the Pipe adv
			discovery.publish(socketService.getPipeAdv());
			discovery.remotePublish(socketService.getPipeAdv());
			
			
			
		} catch (PeerGroupException e) {
			// could not instantiate the group, print the stack and exit
			Log.d(PeerDroidSample.TAG, "fatal error : group creation failure");
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Waiting for rendezvous connection
		while (!rendezvous.isConnectedToRendezVous()) {
			Log.d(PeerDroidSample.TAG, "Waiting for rendezvous connection!!!");
			synchronized (rendezvous) {
				try {
					rendezvous.wait(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		this.connected  = true;
		
		PeerDroidSample.handler.post(new Runnable() {
             public void run() {
             	PeerDroidSample.introDialog.dismiss();
             }
         });
		
		Log.d(PeerDroidSample.TAG, "Hello from : " + netPeerGroup.getPeerName()
				+ " ---> "
				+ netPeerGroup.getPeerID().getUniqueValue().toString());

		Log.d(PeerDroidSample.TAG, "RendezVous connected!!!");
		
		//Create and publish Peer ADV
		createPeerADV();

		rdvId = null;
		Enumeration rdvEnum = rendezvous.getConnectedRendezVous();
		if (rdvEnum != null) {
			while (rdvEnum.hasMoreElements()) {
				rdvId = (PeerID) rdvEnum.nextElement();
				if (rdvId != null)
					break;
			}
			Log.d(PeerDroidSample.TAG, "I am connected to " + rdvId.toString());
		}
	}

	/**
	 * Create and publish an ADV with specific parameters for Peer
	 */
	public void createPeerADV() {

		ModuleSpecAdvertisement msadv = (ModuleSpecAdvertisement) AdvertisementFactory
				.newAdvertisement(ModuleSpecAdvertisement
						.getAdvertisementType());
		ModuleClassID mcID = IDFactory.newModuleClassID();

		// Add WSDL description to Param in ModuleSpecAdv
		StructuredTextDocument param = (StructuredTextDocument) StructuredDocumentFactory
				.newStructuredDocument(new MimeMediaType("text/xml"), "Parm");
		String advContent = "Peer ADV content ";

		msadv.setName(this.instanceName);
		msadv.setCreator("P2P JXTA Test");
		msadv.setModuleSpecID(IDFactory.newModuleSpecID(mcID));
		
		//Sets Description with Current Time, it will be used to order and select peers in List
		msadv.setDescription(""+System.currentTimeMillis());
		msadv.setPipeAdvertisement(socketService.getPipeAdv());
		msadv.setParam(param);

		try {
			discovery.publish(msadv);
			discovery.remotePublish(msadv);
		} catch (IOException e) {
			e.printStackTrace();
		}

		long exptime = discovery.getAdvExpirationTime(msadv);
		Log.d(PeerDroidSample.TAG,"Expiration time for peer spec adv is " + exptime
				+ " ms");
	}
	
	/**
	 * Calls when arrives a DiscoveryEvent
	 */
	public void discoveryEvent(DiscoveryEvent ev) {

		DiscoveryResponseMsg res = ev.getResponse();
		String name = "unknown";

		// Get the responding peer's advertisement
		PeerAdvertisement peerAdv = res.getPeerAdvertisement();
		// some peers may not respond with their peerAdv
		if (peerAdv != null) {
			name = peerAdv.getName();
		}

		Log.d(PeerDroidSample.TAG, "###############################################################################################");
		Log.d(PeerDroidSample.TAG, "Got a Discovery Response ["
				+ res.getResponseCount() + " elements] from peer: " + name);
		//printout each discovered peer
		PeerAdvertisement adv = null;
		Advertisement adv1 = null;
		Enumeration en = res.getAdvertisements();

		if (en != null) {
			while (en.hasMoreElements()) {
				
				adv1 = (Advertisement) en.nextElement();
				
				 if ( adv1 instanceof ModuleSpecAdvertisement) {
			            ModuleSpecAdvertisement app = (ModuleSpecAdvertisement)adv1;
			            Log.d(PeerDroidSample.TAG,"Found MSA Peer Advertisement ----> " + "PeerADV Name:" + app.getName() + " ----> SocketADV ID: " + app.getPipeAdvertisement().getID());
			      
			            try
			            {
			            	Long longvalue = Long.parseLong(app.getDescription());     
			            	Peer newPeer = new Peer(app.getName(),app.getPipeAdvertisement(),longvalue.longValue());
			              	int index = this.peerList.indexOf(newPeer);
			            	
			            	if(index != -1 && peerList.get(index).getLastUpdate() < newPeer.getLastUpdate() && !newPeer.getName().equals(this.instanceName))
			            	{
			            		this.peerList.remove(newPeer);
			            		this.peerList.add(newPeer);	
			            	}
			            	else if(!newPeer.getName().equals(this.instanceName))
			            		this.peerList.add(newPeer);	
			            }
			            catch(NumberFormatException e)
			            {
			            }
				 }
			}
		}
		Log.d(PeerDroidSample.TAG, "###############################################################################################");
	}

	/**
	 * Method used to search available peers 
	 * @param peerName
	 */
	public void startSearchingPeers() {
		//Create a new Thread to manage the discovery
		discoveryThread = new Thread(new PeerDiscoveryHandler(this, ""),
				"Discovery Handler Thread");
		discoveryThread.start();
	}
	
	/**
	 * Stop JXTA network
	 */
	public void stop(){
		stopped = false;
		this.networkManager.stopNetwork();
	}
	

	public DiscoveryService getDiscovery() {
		return discovery;
	}

	public void setDiscovery(DiscoveryService discovery) {
		this.discovery = discovery;
	}

	public boolean isFounded() {
		return founded;
	}

	public void setFounded(boolean founded) {
		this.founded = founded;
	}

	public PeerGroup getNetPeerGroup() {
		return netPeerGroup;
	}

	public void setNetPeerGroup(PeerGroup netPeerGroup) {
		this.netPeerGroup = netPeerGroup;
	}

	public boolean isStopped() {
		return stopped;
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}

	public RendezVousService getRendezvous() {
		return rendezvous;
	}

	public void setRendezvous(RendezVousService rendezvous) {
		this.rendezvous = rendezvous;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public static String getRdvlist() {
		return rdvlist;
	}

	public static void setRdvlist(String rdvlist) {
		JXTAService.rdvlist = rdvlist;
	}

	public PeerID getSocketPeerID() {
		return socketPeerID;
	}

	public void setSocketPeerID(PeerID socketPeerID) {
		this.socketPeerID = socketPeerID;
	}

	public PeerID getRdvId() {
		return rdvId;
	}

	public void setRdvId(PeerID rdvId) {
		this.rdvId = rdvId;
	}

	public SocketService getSocketService() {
		return socketService;
	}

	public void setSocketService(SocketService socketService) {
		this.socketService = socketService;
	}

	public NetworkManager getNetworkManager() {
		return networkManager;
	}

	public void setNetworkManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	public NetworkConfigurator getConfig() {
		return config;
	}

	public void setConfig(NetworkConfigurator config) {
		this.config = config;
	}

	public Thread getDiscoveryThread() {
		return discoveryThread;
	}

	public void setDiscoveryThread(Thread discoveryThread) {
		this.discoveryThread = discoveryThread;
	}

	public Thread getServerThread() {
		return serverThread;
	}

	public void setServerThread(Thread serverThread) {
		this.serverThread = serverThread;
	}

	public String getPrincipal() {
		return principal;
	}

	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

}