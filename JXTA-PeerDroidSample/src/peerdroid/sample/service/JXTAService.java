package peerdroid.sample.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;

import javax.security.cert.CertificateException;

import peerdroid.sample.handler.PeerDiscoveryHandler;
import peerdroid.sample.neighbours.Peer;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.ModuleClassID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.rendezvous.RendezVousService;

/**
 * A simple and re-usabel example for creating and joining a JXTA peergroup
 * 
 * @author Michele Amoretti (ardarico) Marco Picone
 * @created may 18, 2007
 */

public class JXTAService implements DiscoveryListener {

	private DiscoveryService discovery;
	private PeerGroup netPeerGroup = null;
	private boolean stopped = false;
	private RendezVousService rendezvous;
	private String instanceName = "NA";
	private final static File home = new File(System.getProperty("JXTA_HOME",
			"cache"));

	private static String rdvlist = "http://www.ce.unipr.it/~picone/rdvlist.txt";

	private String socketPeerName;
	private PeerID socketPeerID;
	private PeerID rdvId;
	private boolean founded = false;
	private SocketService socketService;
	private NetworkManager networkManager;
	private NetworkConfigurator config;
	private Thread discoveryThread, serverThread;
	private String principal, password;

	private ArrayList<Peer> peerList = new ArrayList<Peer>();
	
	/**
	 * A simple and re-usable example of starting and stopping a JXTA platform
	 * 
	 * @param instanceName
	 *            Node name
	 * @param peerDescriptor 
	 * @param home
	 *            Cache storage home directory
	 */
	public JXTAService(String instanceName, String principal, String password) {

		this.instanceName = instanceName;
		this.principal = principal;
		this.password = password;

		start(principal, password);
	}

	/**
	 * Creates and starts the JXTA NetPeerGroup using a platform configuration
	 * template. This class also registers a listener for rendezvous events
	 * 
	 * @param principal
	 *            principal used the generate the self signed peer root cert
	 * @param password
	 *            the root cert password
	 */
	public void start(String principal, String password) {

		try {

			File instanceHome = new File(home, instanceName);

			// Creating the NetworkManager
			networkManager = new NetworkManager(NetworkManager.ConfigMode.EDGE,
					"My Local Network", instanceHome.toURI());

			// Persisting it to make sure the Peer ID is not re-created each
			// time
			// the NetworkManager is instantiated
			networkManager.setConfigPersistent(true);

			// Retrieving the Network Configurator
			config = networkManager.getConfigurator();

			// NetworkConfigurator config = new NetworkConfigurator();

			config.setHome(instanceHome);
			if (!config.exists()) {
				config.setPeerID(IDFactory
						.newPeerID(PeerGroupID.defaultNetPeerGroupID));
				config.setName(instanceName);
				config.setDescription("Created by AndroidTester");
				config.setMode(NetworkConfigurator.EDGE_NODE);
				config.setPrincipal(principal);
				config.setPassword(password);
				config.setUseMulticast(false);

				try {
					config.addRdvSeedingURI(new URI(rdvlist));
					// config.addRdvSeedingURI(new
					// URI("http://rdv.jxtahosts.net/cgi-bin/rendezvous.cgi?2"
					// ));
					// config.addRelaySeedingURI(new
					// URI("http://rdv.jxtahosts.net/cgi-bin/relays.cgi?2"));
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
					System.out.println("Loading found configuration");
					File LocalConfig = new File(config.getHome(),
							"PlatformConfig");
					config.load(LocalConfig.toURI());
					System.out.println("Configuration loaded");
				} catch (IOException ex) {
					System.out.println("Configuration Loading Exception !!!");
					ex.printStackTrace();
					System.exit(-1);
				} catch (CertificateException e) {
					e.printStackTrace();
				}
			}

			netPeerGroup = networkManager.startNetwork();
			rendezvous = netPeerGroup.getRendezVousService();
			discovery = netPeerGroup.getDiscoveryService();

			// Creo il SocketService
			socketService = new SocketService(netPeerGroup);

		} catch (PeerGroupException e) {
			// could not instantiate the group, print the stack and exit
			System.out.println("fatal error : group creation failure");
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (!rendezvous.isConnectedToRendezVous()) {
			System.out.println("Waiting for rendezvous connection!!!");
			synchronized (rendezvous) {
				try {
					rendezvous.wait(1000);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
			}
		}

		System.out.println("Hello from : " + netPeerGroup.getPeerName()
				+ " ---> "
				+ netPeerGroup.getPeerID().getUniqueValue().toString());

		System.out.println("RendezVous connected!!!");

		createPeerADV();

		rdvId = null;
		Enumeration rdvEnum = rendezvous.getConnectedRendezVous();
		if (rdvEnum != null) {
			while (rdvEnum.hasMoreElements()) {
				rdvId = (PeerID) rdvEnum.nextElement();
				if (rdvId != null)
					break;
			}
			System.out.println("I am connected to " + rdvId.toString());
		}
		
		//this.peerDescriptor.setSocketAddr(this.socketService.getPipeAdv().getPipeID().toString());

	}

	/**
	 * Stop the network
	 */
	public void stop(){
		this.networkManager.stopNetwork();
	}
	
	public void discoveryEvent(DiscoveryEvent ev) {

		Calendar calendar = new GregorianCalendar();

		int ore = calendar.get(Calendar.HOUR_OF_DAY);
		int minuti = calendar.get(Calendar.MINUTE);
		int secondi = calendar.get(Calendar.SECOND);

		String orario = ore + ":" + minuti + ":" + secondi;

		DiscoveryResponseMsg res = ev.getResponse();
		String name = "unknown";

		// Get the responding peer's advertisement
		PeerAdvertisement peerAdv = res.getPeerAdvertisement();
		// some peers may not respond with their peerAdv
		if (peerAdv != null) {
			name = peerAdv.getName();
		}

		System.out.println("###############################################################################################");
		System.out.println("At: " + orario + " : Got a Discovery Response ["
				+ res.getResponseCount() + " elements] from peer: " + name);
		// printout each discovered peer
		PeerAdvertisement adv = null;
		Advertisement adv1 = null;
		Enumeration en = res.getAdvertisements();

		if (en != null) {
			while (en.hasMoreElements()) {
				
				adv1 = (Advertisement) en.nextElement();
				
				 if ( adv1 instanceof ModuleSpecAdvertisement) {
					 
			            ModuleSpecAdvertisement app = (ModuleSpecAdvertisement)adv1;
			            System.out.println("Found MSA Peer Advertisement ----> " + "PeerADV Name:" + app.getName() + " ----> SocketADV ID: " + app.getPipeAdvertisement().getID());
			            
			            try
			            {
			            	Long longvalue = Long.parseLong(app.getDescription());     
			            	Peer newPeer = new Peer(app.getName(),app.getPipeAdvertisement(),longvalue.longValue());
			              	int index = this.peerList.indexOf(newPeer);
			            	
			            	if(index != -1)
			            	{
			            		if(peerList.get(index).getLastUpdate() < newPeer.getLastUpdate() && !newPeer.getName().equals(this.instanceName))
			            		{
			            			this.peerList.remove(newPeer);
			            			this.peerList.add(newPeer);	
			            		}
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
		System.out.println("PEER LIST COUNT = " + peerList.size());
		System.out.println("###############################################################################################");
	}

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
		// msadv.setSpecURI(rps.getAddress()); // this must be used in the
		// discovery phase!
		//msadv.setDescription("Description of Peer ADV");
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
		System.out.println("Expiration time for peer spec adv is " + exptime
				+ " ms");
	}

	public void searchPeerName(String peerName) {
		// Creo un nuovo thread per la gestione della ricerca
		discoveryThread = new Thread(new PeerDiscoveryHandler(this, peerName),
				"Discovery Handler Thread");
		discoveryThread.start();
	}

	public void ceckPeerInLocalCache() {
		try
		{
		if (discovery != null) {

			System.out.println("--- Peer Advertisements in local cache ---");
			PeerAdvertisement adv = null;

			Enumeration en = discovery.getLocalAdvertisements(
					DiscoveryService.PEER, null, null);

			int peerAdvertisement = 0;
			if (en != null) {
				if (!en.hasMoreElements()) {
					System.out.println("No Peer Advertisements in Local Cache.");
				}
				while (en.hasMoreElements()) {

					adv = (PeerAdvertisement) en.nextElement();
					peerAdvertisement++;
					System.out.println("\nPeer Name = " + adv.getName() +
					"\nAdvertisement Expiration Time = " +
					discovery.getAdvExpirationTime(adv));
				}
			}
			else {
				System.out.println("No Adv in Local Cache. Enumeration is null.");
			}
		}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public void ceckADVInLocalCache() {
		try
		{
		if (discovery != null) {


			System.out.println("--- Advertisements in local cache ---");
			Advertisement adv1 = null;
			Enumeration<Advertisement> en = discovery.getLocalAdvertisements(DiscoveryService.ADV, null,	null);
			int counter = 0;

			if (en != null) {
				while (en.hasMoreElements()) {
					adv1 = (Advertisement) en.nextElement();
					counter++;
				System.out.println("\nAdvertisement Class = "
							+ adv1.getClass() +

							"\nAdvertisement Type = " +

							adv1.getAdvType() +

							"\nAdvertisement Expiration Time = " +

							discovery.getAdvExpirationTime(adv1));

				 if ( adv1 instanceof ModuleSpecAdvertisement) {
			            System.out.println("Found advertisement in cache, adding to list");
			            ModuleSpecAdvertisement app = (ModuleSpecAdvertisement)adv1;
			            System.out.println("PeerADV Name:" + app.getName());
			      }
				
				}

			}

			System.out.println("Number of Advertisements in local cache= " + counter );
			System.out.println("\n--- end local cache ---");

		}

		else {

			System.out.println("The Discovery Service of " + " is not set yet.");

		}
		}
		catch(Exception e){
			e.printStackTrace();
		}
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

	public String getSocketPeerName() {
		return socketPeerName;
	}

	public void setSocketPeerName(String socketPeerName) {
		this.socketPeerName = socketPeerName;
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

	public static File getHome() {
		return home;
	}

	public ArrayList<Peer> getPeerList() {
		return peerList;
	}

	public void setPeerList(ArrayList<Peer> peerList) {
		this.peerList = peerList;
	}

}