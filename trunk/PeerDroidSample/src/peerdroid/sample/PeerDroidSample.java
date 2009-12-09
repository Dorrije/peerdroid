package peerdroid.sample;

import java.util.ArrayList;

import peerdroid.sample.dialog.GraphDialog;
import peerdroid.sample.dialog.IntroDialog;
import peerdroid.sample.dialog.NetworkInfoDialog;
import peerdroid.sample.dialog.PeerListDialog;
import peerdroid.sample.dialog.SettingsDialog;
import peerdroid.sample.handler.RandomMessagesHandler;
import peerdroid.sample.handler.SocketServerHandler;
import peerdroid.sample.info.NetworkInformation;
import peerdroid.sample.neighbours.Peer;
import peerdroid.sample.service.JXTAService;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import peerdroid.sample.handler.*;

/**
 * 
 * Sample Application that shows how we can user PeerDroid
 * on Android platform.
 * 
 * @author  Marco Picone picone.m@gmail.com
 * @created 27/08/2009
 */
public class PeerDroidSample extends Activity {
    
	public static String TAG = "PEERDROID-SAMPLE";
	
	/* PeerDroid Sample - GUI Elements */
	private Button sendButton = null;
	public static TextView messagesTextView = null;
	private static JXTAService managerJXTA = null;
	private EditText messageEditText;

	/* Message that will be send to other peers */
	public static String outgoingMessage = "Hello World !";
	
	/*Check if  */
	public static boolean programClosed = false; 
	
	//NetworkInformation object
	private NetworkInformation networkInfo = new NetworkInformation();
	
	/*List of peers received from the RDV */
	private ArrayList<Peer> peerList = new ArrayList<Peer>();
	
	//Listener for Send Button
	private View.OnClickListener sendButton_OnCliclistener = new View.OnClickListener() {
		public void onClick(View view) {
			outgoingMessage = messageEditText.getText().toString();
		}
	};

	/*Dialog Window used to show information about connection to JXTA NetWork  */
	public static IntroDialog introDialog;

	/*Dialog Windows used to show information about NetWork Status*/
	private NetworkInfoDialog infoDialog;

	/*Dialog Window used to show a Graph for Network Speed*/
	private GraphDialog dialogGraph;

	/*Dialog that shows the peerlist received from the RDV */
	private PeerListDialog peerListDialog;

	/*Thread to manage SocketServer of the client*/
	public Thread serverThread = null;

	/*Thread to manage random messages to available peers*/
	public Thread randomMessagesThread = null;

	/*Thread of JXTA Connection*/
	private Thread jxtaThread;
	
	/*Android Handler for PeerDroidSample*/
	public static Handler handler = new Handler();
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.p2pmain);
        
        /*Finds View elements by Id*/
        messagesTextView = (TextView) findViewById(R.id.messagesTextView);
        sendButton = (Button) findViewById(R.id.sendButton);
        messageEditText = (EditText)findViewById(R.id.messageEditText);
        sendButton.setOnClickListener(sendButton_OnCliclistener);

        Button clearButton = (Button) findViewById(R.id.clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	messagesTextView.setText("");
            } });
        	

		//New Objects for the Dialog Windows
		infoDialog = new NetworkInfoDialog(this);
		dialogGraph = new GraphDialog(this);
		peerListDialog = new PeerListDialog(this);
		
		jxtaThread = new Thread() {
			public void run() {
					managerJXTA = new JXTAService("Ubuntu-Piko" ,"principal","password",getFileStreamPath("jxta"));
					managerJXTA.peerList = peerList;
					managerJXTA.startSearchingPeers();
					 
					serverThread = new Thread(new SocketServerHandler(managerJXTA.getSocketService()), "Connection Handler Thread");
					serverThread.start();
					 
					randomMessagesThread = new Thread(new RandomMessagesHandler(managerJXTA),"Random Message Thread");
					randomMessagesThread.start();
				}
		   };
		   jxtaThread.start();
		
        //Initialize and start the Introduction Dialog Window, that initialize and start the JXTA System
		introDialog = new IntroDialog(this);
		introDialog.show();
    }
    

	@Override
	/**
	 * Called when the OptionsMenu was created
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		//Add new voices, with name and icon, to Activity Menu
		menu.add(0, 0,0, "Network Info").setIcon(R.drawable.network_48);
		menu.add(1, 1, 1, "Network Graph").setIcon(R.drawable.graph_48);
		menu.add(2,2, 2, "Peer List").setIcon(R.drawable.network_48);

		return true;
	}

	@Override
	/**
	 * Called when OptionsItem is Selected
	 */
	public boolean onOptionsItemSelected(MenuItem item){

		switch (item.getGroupId()) {
		case 0:
			//Start InfoDialog Window
			infoDialog.show();
			return true;
		case 1:
			//Start NetworkInfo Window
			dialogGraph.setNetworkInfo(networkInfo);
			dialogGraph.show();
			return true;
		case 2:
			peerListDialog.setPeerList(peerList);
			peerListDialog.show();
		return true;
		}
		return false;

	}

    
	@Override
	/**
	 * Called on activity destroyed
	 */
	protected void onDestroy() {

		super.onDestroy();
		
		//Stop JXTA Network
		managerJXTA.stop();
		
		//Suspend different Threads
		serverThread.suspend();
		randomMessagesThread.suspend();
		jxtaThread.suspend();
		
		Log.d(TAG,"onDestroy CALL !");
	}
    
}