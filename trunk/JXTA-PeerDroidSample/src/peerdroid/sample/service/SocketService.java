package peerdroid.sample.service;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import peerdroid.sample.handler.ConnectionHandler;

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

	public final static String SOCKETIDSTR = "urn:jxta:uuid-59616261646162614E5047205032503393B5C2F6CA7A41FBB0F890173088E79404";

	/**
	 * Class Constructor, init variable and creates Socket Advertisement
	 * @param netPeerGroup
	 */
	public SocketService(PeerGroup netPeerGroup) {

		System.out.println("SocketService Costructor CALL");

		this.netPeerGroup = netPeerGroup;

		this.SocketTAG = "SOCKET SERVICE ( " + this.netPeerGroup.getPeerName()
				+ " ): ";

		pipeAdv = createSocketAdvertisement();
		System.out.println("SocketService PipeADV: " + pipeAdv);

	}

	/**
	 * Creates new JXTA ServerSocket from netPeerGroup and socketAdv.
	 */
	public void createSocket() {

		System.out.println(SocketTAG + "Starting ServerSocket");
		serverSocket = null;
		try {
			serverSocket = new JxtaServerSocket(netPeerGroup,
					pipeAdv, 10);
			// Log.d(PeerDroidSample.TAG,SocketTAG + "Addr: " +
			// serverSocket.getInetAddress() + " \n" +
			// serverSocket.getLocalSocketAddress() );
			// serverSocket.setSoTimeout(0);
		} catch (IOException e) {
			System.out.println(SocketTAG
					+ "failed to create a server socket");
			e.printStackTrace();
			System.exit(-1);
		}

		while (true) {
			try {
				//System.out.println(SocketTAG
					//	+ "Waiting for connections on PORT: "
						//+ serverSocket.getLocalPort());
				Socket socket = serverSocket.accept();
				if (socket != null) {
					//System.out.println(SocketTAG
						//	+ "New socket connection accepted");
					Thread thread = new Thread(new ConnectionHandler(socket),
							"Connection Handler Thread");
					thread.start();
				}
			} catch (Exception e) {
				System.err.println(SocketTAG+e.getLocalizedMessage());
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
			System.out.println(SocketTAG
					+ "Connecting to the server ---> PipeADV : " + pipeAdvertisement.getID());
			socket = new JxtaSocket(netPeerGroup, pipeAdvertisement, 5000);

			if (socket == null) {
				System.err.println(SocketTAG + "Socket NULL !! ");
			}

		} catch (IOException io) {
			return_value = -1;
			System.err.println("EXCEPTION ConnectToSocket : "+ io.getLocalizedMessage());
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

		System.out.println(SocketTAG + "SocketService SendMessage CALL !");

		int server_ack = -1;

		if(socket == null)
			return server_ack;
		
		try {
			// get the socket output stream
			OutputStream out = socket.getOutputStream();
			
			// get the socket input stream
			InputStream in = socket.getInputStream();

			// Sends message length
			out.write(message.getBytes().length);

			long current = 0;

			System.out.println(SocketTAG + "Sending Message : "
					+ message + " size: " + message.getBytes().length);

			while (current < 1) {

				byte[] out_buf = message.getBytes();

				//System.out.println(SocketTAG + "Out-Buffer: "
					//	+ new String(out_buf) + " Len: " + out_buf.length);

				out.write(out_buf);
				out.flush();

				server_ack = in.read();

				if (server_ack != -1)
					System.out.println(SocketTAG
							+ "Message Send ---> ServerACK : " + server_ack);
				else
					System.out.println(SocketTAG
							+ "ERROR Sending Message ---> ServerACK : "
							+ server_ack);

				current++;
			}
			out.close();
			in.close();

			long finish = System.currentTimeMillis();
			long elapsed = finish - start;

			System.out.println(SocketTAG + "Comunication Time: "
							+ elapsed);

		} catch (IOException io) {
			System.err.println(SocketTAG + "EXCEPTION: "
					+ io.getLocalizedMessage());
		}

		return server_ack;
	}

	public void closeConnection() {

		try {
			socket.close();
			System.out.println(SocketTAG + "Socket connection closed");

		} catch (IOException e) {
			System.err.println(SocketTAG + "Error Closing Socket");
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
		PipeAdvertisement pipeAdv = (PipeAdvertisement) AdvertisementFactory
				.newAdvertisement(PipeAdvertisement.getAdvertisementType());
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
