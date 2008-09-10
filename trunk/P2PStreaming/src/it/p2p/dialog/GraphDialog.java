package it.p2p.dialog;

import it.p2p.P2PStreaming;
import it.p2p.graph.GraphView;
import it.p2p.info.NetworkInformation;

import java.net.ConnectException;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 *  P2P Streaming Player for Google Android
 *  
 *  Android Dialog Windows to show NetworkStatistics Graph.
 *
 * @author   Marco Picone 
 * @created  01/09/2008
 */
public class GraphDialog extends Dialog {
   
	//Variable that contains all networkInformation
	private NetworkInformation networkInfo;
	
	//Main Android Activity
	private P2PStreaming activity;

	/**
	 * Class constructor
	 */	 
	public GraphDialog( P2PStreaming activity) {
       super(activity);
       this.activity = activity;
    }

	/**
	 * Method call when the Dialog Window start
	 */
    protected void onStart() {
        super.onStart();
        setTitle("Network Graph");
        
        //New object used to create and draw the graph, it's based on networkInfo datas
        GraphView graphView = new GraphView(activity);
        graphView.setNetworkInfo(networkInfo);
        
        setContentView(graphView);
    }

	public NetworkInformation getNetworkInfo() {
		return networkInfo;
	}

	public void setNetworkInfo(NetworkInformation networkInfo) {
		this.networkInfo = networkInfo;
	}
  
}
