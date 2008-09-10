package it.p2p.service;

import it.p2p.P2PStreaming;
import it.p2p.handler.ConnectionHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

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

	public final static String SOCKETIDSTR = "urn:jxta:uuid-59616261646162614E5047205032503393B5C2F6CA7A41FBB0F890173088E79404";

	/**
	 * Class Constructor, init variable and creates Socket Advertisement
	 * @param netPeerGroup
	 */
	public SocketService(PeerGroup netPeerGroup) {

		Log.d(P2PStreaming.TAG, "SocketService Costructor CALL");

		this.netPeerGroup = netPeerGroup;

		this.SocketTAG = "SOCKET SERVICE ( " + this.netPeerGroup.getPeerName()
				+ " ): ";

		pipeAdv = createSocketAdvertisement();
		Log.d(P2PStreaming.TAG, "SocketService PipeADV: " + pipeAdv);

	}

	/**
	 * Creates new JXTA ServerSocket from netPeerGroup and socketAdv.
	 */
	public void createSocket() {

		Log.d(P2PStreaming.TAG, SocketTAG + "Starting ServerSocket");
		serverSocket = null;
		try {
			serverSocket = new JxtaServerSocket(netPeerGroup,
					createSocketAdvertisement(), 10);
			// Log.d(P2PStreaming.TAG,SocketTAG + "Addr: " +
			// serverSocket.getInetAddress() + " \n" +
			// serverSocket.getLocalSocketAddress() );
			// serverSocket.setSoTimeout(0);
		} catch (IOException e) {
			Log.d(P2PStreaming.TAG, SocketTAG
					+ "failed to create a server socket");
			e.printStackTrace();
			System.exit(-1);
		}

		while (true) {
			try {
				Log.d(P2PStreaming.TAG, SocketTAG
						+ "Waiting for connections on PORT: "
						+ serverSocket.getLocalPort());
				Socket socket = serverSocket.accept();
				if (socket != null) {
					Log.d(P2PStreaming.TAG, SocketTAG
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
	public int connectToSocket(PeerID peerid) {

		int return_value = 0;

		try {

			start = System.currentTimeMillis();
			Log.d(P2PStreaming.TAG, SocketTAG
					+ "Connecting to the server ---> PeerID : " + peerid);
			socket = new JxtaSocket(netPeerGroup, peerid, pipeAdv, 5000, true);

			if (socket == null) {
				Log.d(P2PStreaming.TAG, SocketTAG + "Socket NULL !! ");
			}

		} catch (IOException io) {
			return_value = -1;
			Log.d(P2PStreaming.TAG, "EXCEPTION ConnectToSocket : "
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

		Log.d(P2PStreaming.TAG, SocketTAG + "SocketService SendMessage CALL !");

		int server_ack = -1;

		try {
			// get the socket output stream
			OutputStream out = socket.getOutputStream();

			// get the socket input stream
			InputStream in = socket.getInputStream();

			// Sends message length
			out.write(message.getBytes().length);

			long current = 0;

			Log.d(P2PStreaming.TAG, SocketTAG + "Invio il messaggio : "
					+ message + " size: " + message.getBytes().length);

			while (current < 1) {

				byte[] out_buf = message.getBytes();

				Log.d(P2PStreaming.TAG, SocketTAG + "Out-Buffer: "
						+ new String(out_buf) + " Len: " + out_buf.length);

				out.write(out_buf);
				out.flush();

				server_ack = in.read();

				if (server_ack != -1)
					Log.d(P2PStreaming.TAG, SocketTAG
							+ "Message Send ---> ServerACK : " + server_ack);
				else
					Log.d(P2PStreaming.TAG, SocketTAG
							+ "ERROR Sending Message ---> ServerACK : "
							+ server_ack);

				current++;
			}
			out.close();
			in.close();

			long finish = System.currentTimeMillis();
			long elapsed = finish - start;

			Log
					.d(P2PStreaming.TAG, SocketTAG + "Comunication Time: "
							+ elapsed);

		} catch (IOException io) {
			Log.d(P2PStreaming.TAG, SocketTAG + "EXCEPTION: "
					+ io.getLocalizedMessage());
		}

		return server_ack;
	}

	public void closeConnection() {

		try {
			socket.close();
			Log.d(P2PStreaming.TAG, SocketTAG + "Socket connection closed");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public PipeAdvertisement createSocketAdvertisement() {

		byte[] preCookedPID = { (byte) 0xD1, (byte) 0xD1, (byte) 0xD1,
				(byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1,
				(byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1,
				(byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xD1 };

		PipeID id = (PipeID) IDFactory.newPipeID(netPeerGroup.getPeerGroupID(),
				preCookedPID);
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
