package peerdroid.sample.dialog;


import java.io.File;
import java.net.ConnectException;
import java.util.ArrayList;

import peerdroid.sample.PeerDroidSample;
import peerdroid.sample.R;
import peerdroid.sample.graph.GraphView;
import peerdroid.sample.handler.RandomMessagesHandler;
import peerdroid.sample.handler.SocketServerHandler;
import peerdroid.sample.neighbours.Peer;
import peerdroid.sample.service.JXTAService;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 *  P2P Streaming Player for Google Android
 *  
 *  Android Dialog Windows used as an introduction of application.
 *  It deals to initialize the JXTA System.
 *
 * @author   Marco Picone 
 * @created  01/09/2008
 */
public class IntroDialog extends Dialog {
   
	private PeerDroidSample activity;
	private ProgressBar progressBar;
	private TextView 	introTextView;
	

	/**
	 * Class Constructor
	 * 
	 * @param activity
	 */
	public IntroDialog( PeerDroidSample activity) {
		
       super(activity);
       this.activity = activity;    
	}
	
	/**
	 * Method call when the Dialog Window start
	 */
    protected void onStart() {
    	
        super.onStart();
        setTitle("Android - PeerDroid");  
        setContentView(R.layout.intro);
        
        progressBar = (ProgressBar) findViewById(R.id.introProgressBar);
        introTextView = (TextView) findViewById(R.id.introTextView);
    
    }
  
}
