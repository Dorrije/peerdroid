package peerdroid.sample.dialog;

import java.net.ConnectException;

import peerdroid.sample.PeerDroidSample;
import peerdroid.sample.graph.GraphView;
import peerdroid.sample.info.NetworkInformation;

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
	private PeerDroidSample activity;

	/**
	 * Class constructor
	 */	 
	public GraphDialog( PeerDroidSample activity) {
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
