package it.p2p.handler;


import it.p2p.P2PStreaming;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.util.Log;

/**
 *  P2P Streaming Player for Google Android
 *  
 *  Connection Handler used to send and receive message over socket.
 *
 * @author   Marco Picone 
 * @created  01/09/2008
 */
public class ConnectionHandler implements Runnable {
	
	private String TAG = "CONNECTION HANDLER ";
	
    Socket socket = null;

    /**
     * Class constructor
     * @param socket
     */
    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * Sends and receive data over socket
     *
     * @param socket the socket
     */
    private void sendAndReceiveData(Socket socket) {
        try {
        	
            long start = System.currentTimeMillis();

            // get the socket output stream
            OutputStream out = socket.getOutputStream();
            // get the socket input stream
            InputStream in = socket.getInputStream();
            
            long current = 0;

            Log.d(P2PStreaming.TAG,TAG + "Socket: Start Comunication ...");
            
            int size = in.read();
            
            Log.d(P2PStreaming.TAG,TAG + "Size to read: " + size);
            
            if( size > 0 )
            {
            	byte[] buf = new byte[size];
            
            	while (current < 1) {
                 
            	Log.d(P2PStreaming.TAG,TAG + "Sono nel While! ");

            	int read = in.read(buf);
                
            	int ack_value;
            	
            	if( read > 0 )
            		ack_value = 1;
            	else
            		ack_value = -1;
            	
            	
                out.write(ack_value);
                out.flush();
                current++;
                
                Log.d(P2PStreaming.TAG,TAG + "Ho letto : " + new String(buf) + " Size : " + read);
            	}
            	
            
            	out.close();
            	in.close();

            	long finish = System.currentTimeMillis();
            	long elapsed = finish - start;
            
            	Log.d(P2PStreaming.TAG,TAG + "Socket elapsed time: " + elapsed);
            
            	socket.close();
            	Log.d(P2PStreaming.TAG,TAG + "Socket closed");
            }
            else{
            	Log.d(P2PStreaming.TAG,TAG + "SIZE ERROR ---> Comunicazione non riuscita !");	
            }
        } catch (Exception ie) {
            Log.d(P2PStreaming.TAG,TAG + "Eccezione: " + ie.getLocalizedMessage());
        	//ie.printStackTrace();
        }
    }

    /**
     * Thread run method
     */
    public void run() {
        sendAndReceiveData(socket);
    }
}
