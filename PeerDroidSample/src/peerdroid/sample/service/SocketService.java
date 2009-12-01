package peerdroid.sample.service;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import peerdroid.sample.PeerDroidSample;
import peerdroid.sample.handler.ConnectionHandler;

import android.util.Log;

import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaServerSocket;
import net.jxta.socket.JxtaSocket;

/**
 * P2P Streaming Player for Google Android
 * 
 * Class used to manage all JXTA Socket service.
 * 
 * @author Marco Picone
 * @created 01/09/2008
 */
public class SocketService {

	// Queste due variabili prima erano transient
	private PeerGroup netPeerGroup = null;
	private PipeAdvertisement pipeAdv = null;

	private JxtaSocket socket;

	private JxtaServerSocket serverSocket;

	private String SocketTAG = "";

	long start;
	private JXTAService jxtaService = null;

	public final static String SOCKETIDSTR = "urn:jxta:uuid-59616261646162614E5047205032503393B5C2F6CA7A41FBB0F890173088E79404";

	/**
	 * Class Constructor, init variable and creates Socket Advertisement
	 * @param jxtaService.getNetPeerGroup()
	 */
	public SocketService(JXTAService jxtaService) {

		Log.d(PeerDroidSample.TAG, "SocketService Costructor CALL");

		this.netPeerGroup = jxtaService.getNetPeerGroup();
		this.jxtaService  = jxtaService;

		this.SocketTAG = "SOCKET SERVICE ( " + this.netPeerGroup.getPeerName()
				+ " ): ";

		pipeAdv = createSocketAdvertisement();
		Log.d(PeerDroidSample.TAG, "SocketService PipeADV: " + pipeAdv);

	}

	/**
	 * Creates new JXTA ServerSocket from netPeerGroup and socketAdv.
	 */
	public void createSocket() {

		Log.d(PeerDroidSample.TAG, SocketTAG + "Starting ServerSocket");
		serverSocket = null;
		try {
			serverSocket = new JxtaServerSocket(netPeerGroup,
					pipeAdv, 100);
			// Log.d(PeerDroidSample.TAG,SocketTAG + "Addr: " +
			// serverSocket.getInetAddress() + " \n" +
			// serverSocket.getLocalSocketAddress() );
			//serverSocket.setSoTimeout(0);
		} catch (IOException e) {
			Log.d(PeerDroidSample.TAG, SocketTAG
					+ "failed to create a server socket");
			e.printStackTrace();
			System.exit(-1);
		}

		while (!this.jxtaService.isStopped()) {
			try {
				Log.d(PeerDroidSample.TAG, SocketTAG
						+ "Waiting for connections on PORT: "
						+ serverSocket.getLocalPort());
				Socket socket = serverSocket.accept();
				if (socket != null) {
					Log.d(PeerDroidSample.TAG, SocketTAG
							+ "New socket connection accepted");
					Thread thread = new Thread(new ConnectionHandler(socket),
							"Connection Handler Thread");
					thread.start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Connects client to ServerSocket with specific pipeADV and peerName
	 * 
	 * @param peerid
	 * @return
	 */
	public int connectToSocket(PipeAdvertisement pipeAdvertisement) {

		int return_value = 0;

		try {

			start = System.currentTimeMillis();
			Log.d(PeerDroidSample.TAG, SocketTAG
					+ "Connecting to the server ---> PipeADV : " + pipeAdvertisement.getID());
			socket = new JxtaSocket(netPeerGroup, pipeAdvertisement, 5000);

			if (socket == null) {
				Log.d(PeerDroidSample.TAG, SocketTAG + "Socket NULL !! ");
			}

		} catch (IOException io) {
			return_value = -1;
			Log.d(PeerDroidSample.TAG, "EXCEPTION ConnectToSocket : "
					+ io.getLocalizedMessage());
			return return_value;
		}

		return return_value;
	}

	/**
	 * Sends message over socket
	 * @param message
	 * @return
	 */
	public int sendMessage(String message) {

		Log.d(PeerDroidSample.TAG, SocketTAG + "SocketService SendMessage CALL !");

		int server_ack = -1;

		if(socket == null)
			return -1;
		
		try {
			// get the socket output stream
			OutputStream out = socket.getOutputStream();

			// get the socket input stream
			InputStream in = socket.getInputStream();

			// Sends message length
			out.write(message.getBytes().length);

			long current = 0;

			Log.d(PeerDroidSample.TAG, SocketTAG + "Invio il messaggio : "
					+ message + " size: " + message.getBytes().length);

			while (current < 1) {

				byte[] out_buf = message.getBytes();

				Log.d(PeerDroidSample.TAG, SocketTAG + "Out-Buffer: "
						+ new String(out_buf) + " Len: " + out_buf.length);

				out.write(out_buf);
				out.flush();

				server_ack = in.read();

				if (server_ack != -1)
					Log.d(PeerDroidSample.TAG, SocketTAG
							+ "Message Send ---> ServerACK : " + server_ack);
				else
					Log.d(PeerDroidSample.TAG, SocketTAG
							+ "ERROR Sending Message ---> ServerACK : "
							+ server_ack);

				current++;
			}
			out.close();
			in.close();

			long finish = System.currentTimeMillis();
			long elapsed = finish - start;

			Log
					.d(PeerDroidSample.TAG, SocketTAG + "Comunication Time: "
							+ elapsed);

		} catch (IOException io) {
			Log.d(PeerDroidSample.TAG, SocketTAG + "EXCEPTION: "
					+ io.getLocalizedMessage());
		}

		return server_ack;
	}

	public void closeConnection() {

		try {
			socket.close();
			Log.d(PeerDroidSample.TAG, SocketTAG + "Socket connection closed");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public PipeAdvertisement createSocketAdvertisement() {

		byte[] preCookedPID = { (byte) 0xD1, (byte) 0xD1, (byte) 0xD1,
				(byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1,
				(byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1,
				(byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1 };

		//PipeID id = (PipeID) IDFactory.newPipeID(netPeerGroup.getPeerGroupID(),
			//	preCookedPID);
		PipeID id = (PipeID) IDFactory.newPipeID(netPeerGroup.getPeerGroupID());
		PipeAdvertisement pipeAdv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
		
		//Start Ling Edit
		/*
		PipeID socketID = null;
	    try {
	           socketID = (PipeID) IDFactory.fromURI(new URI(SOCKETIDSTR));
	        } catch (URISyntaxException use) {
	          use.printStackTrace();
	        }
	    pipeAdv.setPipeID(socketID);
		*/ 
		//End Ling Edit
		pipeAdv.setPipeID(id);
		
		// the name really does not matter here, only for illustration
		pipeAdv.setName("test");
		pipeAdv.setType(PipeService.UnicastType);
		return pipeAdv;

		
		/*
		 * PipeID socketID = null;
		 * 
		 * try { socketID = (PipeID) IDFactory.fromURI(new URI(SOCKETIDSTR)); }
		 * catch (URISyntaxException use) { use.printStackTrace(); }
		 * PipeAdvertisement advertisement = (PipeAdvertisement)
		 * AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
		 * 
		 * advertisement.setPipeID(socketID);
		 * advertisement.setType(PipeService.UnicastType);
		 * advertisement.setName("Socket-MyPeer"); return advertisement;
		 */
	}

	public PipeAdvertisement getPipeAdv() {
		return pipeAdv;
	}

	public void setPipeAdv(PipeAdvertisement pipeAdv) {
		this.pipeAdv = pipeAdv;
	}

}
