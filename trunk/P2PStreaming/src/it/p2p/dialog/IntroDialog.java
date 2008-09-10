package it.p2p.dialog;

import it.p2p.P2PStreaming;
import it.p2p.R;
import it.p2p.graph.GraphView;
import it.p2p.service.JXTAService;

import java.io.File;
import java.net.ConnectException;

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
   
	private P2PStreaming activity;
	private ProgressBar progressBar;
	private TextView 	introTextView;
	
	//JXTA variables
	private JXTAService managerJXTA; //Manager of JXTAService
	private File instanceHome; //File refers to the home directory of Android application
	private String peerName; //JXTA Peer Name

	/**
	 * Class Constructor
	 * 
	 * @param activity
	 * @param managerJXTA
	 * @param instanceHome
	 * @param peerName
	 */
	public IntroDialog( P2PStreaming activity, JXTAService managerJXTA, File instanceHome, String peerName) {
		
       super(activity);
       this.activity = activity;
       this.managerJXTA = managerJXTA;
       this.instanceHome = instanceHome;
       this.peerName = peerName;
    
	}
	
	/**
	 * Method call when the Dialog Window start
	 */
    protected void onStart() {
    	
        super.onStart();
        setTitle("Android P2P Live Streaming");  
        setContentView(R.layout.intro);
        
        progressBar = (ProgressBar) findViewById(R.id.introProgressBar);
        introTextView = (TextView) findViewById(R.id.introTextView);
        
        //Creates a new Thread to manage the JXTA system
        Thread app = new Thread() {
			public void run() {
				 managerJXTA = new JXTAService(peerName ,"principal","password",instanceHome,"MyPeer");
				 dismiss();
			}
		};
        
		//Starts the new Thread
		app.start();
       
    }
  
}
