package it.p2p.service;

import it.p2p.P2PStreaming;
import it.p2p.handler.PeerDiscoveryHandler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

import javax.security.cert.CertificateException;

import android.util.Log;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.ConfigurationFactory;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.rendezvous.RendezVousService;

/**
 * 
 *  P2P Streaming Player for Google Android
 * 
 *  Class used to manage all service linked with the JXTA System
 *
 * @author     Marco Picone & Michele Amoretti (ardarico)
 * @created    01/09/2008
 */

public class JXTAService implements DiscoveryListener {

	private DiscoveryService discovery;
	private PeerGroup netPeerGroup = null;
	private boolean stopped = false;
	private RendezVousService rendezvous;
	private String instanceName = "NA";
	private static String rdvlist = "http://dsg.ce.unipr.it/research/SP2A/rdvlist2.txt";
	private String socketPeerName;
	private PeerID socketPeerID;
	private PeerID rdvId;
	private boolean founded = false;
	private SocketService socketService;
	private NetworkManager networkManager;
	private NetworkConfigurator config;
	private Thread discoveryThread, serverThread;
	private String principal, password;
	private File instanceHome;

	/**
	 * Class constructor, sets variables and starts JXTA Platform 
	 * @param instanceName
	 * @param principal
	 * @param password
	 * @param instanceHome
	 * @param socketPeerName
	 */
	public JXTAService(String instanceName, String principal, String password,
			File instanceHome, String socketPeerName) {

		Log.d(P2PStreaming.TAG, "JXTAService Costructor !");

		this.instanceName = instanceName;
		this.principal = principal;
		this.password = password;
		this.instanceHome = instanceHome;
		this.socketPeerName = socketPeerName;

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

			//NetworkConfigurator config = new NetworkConfigurator();

			//JXTASystem.init();

			config.setHome(instanceHome);
			if (!config.exists()) {

				config.setPeerID(IDFactory
						.newPeerID(PeerGroupID.defaultNetPeerGroupID));
				//config.setPeerID(JXTASystem.getMY_ID());
				Log.d(P2PStreaming.TAG, "PeerID Impostato: "
						+ JXTASystem.getMY_ID());
				config.setName(instanceName);
				config.setDescription("Created by AndroidTester");
				config.setMode(NetworkConfigurator.EDGE_NODE);
				config.setPrincipal(principal);
				config.setPassword(password);
				config.setUseMulticast(false);
				try {
					config.addRdvSeedingURI(new URI(rdvlist));
					//config.addRdvSeedingURI(new URI("http://rdv.jxtahosts.net/cgi-bin/rendezvous.cgi?2"));
					//config.addRelaySeedingURI(new URI("http://rdv.jxtahosts.net/cgi-bin/relays.cgi?2"));
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
					Log.d(P2PStreaming.TAG, "Loading found configuration");
					File LocalConfig = new File(config.getHome(),
							"PlatformConfig");
					config.load(LocalConfig.toURI());
					Log.d(P2PStreaming.TAG, "Configuration loaded");
				} catch (IOException ex) {
					Log.d(P2PStreaming.TAG,
							"Configuration Loading Exception !!!");
					ex.printStackTrace();
					System.exit(-1);
				} catch (CertificateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			ConfigurationFactory.setRelayed(true);
			ConfigurationFactory.setName(instanceName);
			ConfigurationFactory.setTCPPortRange(9701, 9799);
			ConfigurationFactory.setPeerID(config.getPeerID());
			ConfigurationFactory.setRdvSeedingURI(new URI(
					"http://dsg.ce.unipr.it/research/SP2A/rdvlist2.txt"));

			ConfigurationFactory factory = ConfigurationFactory.newInstance();
			factory.setUseOnlyRelaySeeds(true);
			factory.setUseOnlyRendezvousSeeds(true);

			netPeerGroup = networkManager.startNetwork();

			rendezvous = netPeerGroup.getRendezVousService();

			discovery = netPeerGroup.getDiscoveryService();

			//Creates SocketService from NetPeerGroup
			socketService = new SocketService(netPeerGroup);

			//Publish the Pipe adv
			discovery.publish(socketService.getPipeAdv());
			discovery.remotePublish(socketService.getPipeAdv());

		} catch (PeerGroupException e) {
			// could not instantiate the group, print the stack and exit
			Log.d(P2PStreaming.TAG, "fatal error : group creation failure");
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Waiting for rendezvous connection
		while (!rendezvous.isConnectedToRendezVous()) {
			Log.d(P2PStreaming.TAG, "Waiting for rendezvous connection!!!");
			synchronized (rendezvous) {
				try {
					rendezvous.wait(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		Log.d(P2PStreaming.TAG, "Hello from : " + netPeerGroup.getPeerName()
				+ " ---> "
				+ netPeerGroup.getPeerID().getUniqueValue().toString());

		Log.d(P2PStreaming.TAG, "RendezVous connected!!!");

		rdvId = null;
		Enumeration rdvEnum = rendezvous.getConnectedRendezVous();
		if (rdvEnum != null) {
			while (rdvEnum.hasMoreElements()) {
				rdvId = (PeerID) rdvEnum.nextElement();
				if (rdvId != null)
					break;
			}
			Log.d(P2PStreaming.TAG, "I am connected to " + rdvId.toString());
		}
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

		Log.d(P2PStreaming.TAG, "Got a Discovery Response ["
				+ res.getResponseCount() + " elements] from peer: " + name);
		//printout each discovered peer
		PeerAdvertisement adv = null;
		Enumeration en = res.getAdvertisements();

		if (en != null) {
			while (en.hasMoreElements()) {
				adv = (PeerAdvertisement) en.nextElement();
				Log.d(P2PStreaming.TAG, " Peer name = " + adv.getName());

				if (adv.getName().equals(socketPeerName)) {
					Log
							.d(P2PStreaming.TAG,
									" Ho trovato il Peer che cercavo !");

					founded = true;

					Log.d(P2PStreaming.TAG, " Imposto il booleano !");

					try {
						socketPeerID = adv.getPeerID();
					} catch (Exception e) {
						Log.d(P2PStreaming.TAG, " Eccezione: "
								+ e.getLocalizedMessage());
						e.printStackTrace();
					}

					Log.d(P2PStreaming.TAG, " Ricavo Descrizione: "
							+ adv.getDescription());
					Log.d(P2PStreaming.TAG, " Ricavo il PeerID: "
							+ this.socketPeerID);

				}

			}
		}
	}

	/**
	 * Method used to search a Peer by Name
	 * @param peerName
	 */
	public void searchPeerName(String peerName) {
		//Create a new Thread to manage the discovery
		discoveryThread = new Thread(new PeerDiscoveryHandler(this, peerName),
				"Discovery Handler Thread");
		discoveryThread.start();
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

}