package peerdroid.sample.handler;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import peerdroid.sample.PeerDroidSample;
import peerdroid.sample.service.JXTAService;
import peerdroid.sample.tools.Utility;

import android.util.Log;

/**
 *  Connection Handler for socket comunications 
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

            
            int size = in.read();
            
            
            if( size > 0 )
            {
            	byte[] buf = new byte[size];
            
            	while (current < 1) {
                 
            	int read = in.read(buf);
                
            	int ack_value;
            	
            	if( read > 0 )
            		ack_value = 1;
            	else
            		ack_value = -1;
            	
            	
                out.write(ack_value);
                out.flush();
                current++;
                
                Log.d(PeerDroidSample.TAG,TAG + "MESSAGE READ : " + new String(buf) + " Size : " + read);
                final String message = new String(buf);
                
                PeerDroidSample.handler.post(new Runnable() {
                    public void run() {
                   	 PeerDroidSample.messagesTextView.setText
                    	(PeerDroidSample.messagesTextView.getText()+"\n"+ 
                    			Utility.getCurrentTime() +
                    			"Message Received: " + message + "\n");
                    	}
                	});
                
                /*
                PeerDroidSample.handler.post(new Runnable() {
                    public void run() {
                    	PeerDroidSample.messagesTextView.setText(PeerDroidSample.messagesTextView.getText()+"\n"+message);
                    }
                });
                */
                
            	}
            	
            
            	out.close();
            	in.close();

            	long finish = System.currentTimeMillis();
            	long elapsed = finish - start;
            
            	Log.d(PeerDroidSample.TAG,TAG + "Socket elapsed time: " + elapsed);
            
            	socket.close();
            	Log.d(PeerDroidSample.TAG,TAG + "Socket closed");
            }
            else{
            	Log.d(PeerDroidSample.TAG,TAG + "SIZE ERROR ---> ERROR SENDING MESSAGE !");	
            }
        } catch (Exception ie) {
            Log.d(PeerDroidSample.TAG,TAG + "EXCEPTION: " + ie.getLocalizedMessage());
        }
    }

    /**
     * Thread run method
     */
    public void run() {
        sendAndReceiveData(socket);
    }
}
