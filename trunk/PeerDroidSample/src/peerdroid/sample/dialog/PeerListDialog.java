package peerdroid.sample.dialog;

import java.util.ArrayList;

import peerdroid.sample.PeerDroidSample;
import peerdroid.sample.R;
import peerdroid.sample.neighbours.Peer;
import android.app.Dialog;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

/**
 *  Dialog Window to show available peers
 *
 * @author   Marco Picone 
 * @created  27/08/2009
 */
public class PeerListDialog extends Dialog {
   
	private PeerDroidSample activity;
	private TextView peerListTextView = null;
	private ArrayList<Peer> peerList = new ArrayList<Peer>();

	/**
	 * Class constructor
	 * @param activity
	 */
	public PeerListDialog( PeerDroidSample activity) {
       super(activity);
       this.activity = activity;
    }

	/**
	 * Method call when the Dialog Window start
	 */
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.peer_list);
        getWindow().setFlags(4, 4);
        setTitle("Peer List");    
        
        peerListTextView = (TextView) findViewById(R.id.peerListText);
       
        Log.d(PeerDroidSample.TAG,"Peer List: "+peerList.size());
        
        for(int i=0; i < peerList.size(); i++)
        	 peerListTextView.setText(peerListTextView.getText()+"\n-->" + peerList.get(i).getName());   
    }

	public void setPeerList(ArrayList<Peer> peerList) {
		// TODO Auto-generated method stub
		this.peerList = peerList;
	}
  
}
