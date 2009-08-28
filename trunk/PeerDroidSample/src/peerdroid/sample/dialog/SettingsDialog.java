package peerdroid.sample.dialog;


import java.net.ConnectException;

import peerdroid.sample.PeerDroidSample;
import peerdroid.sample.R;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 *  P2P Streaming Player for Google Android
 *  
 *  Android Dialog Windows used to set the Streaming Server Path.
 *
 * @author   Marco Picone 
 * @created  01/09/2008
 */
public class SettingsDialog extends Dialog {
   
	
    private EditText serverText;
	private PeerDroidSample activity;

	/**
	 * Class Constructor 
	 * @param activity
	 */
	public SettingsDialog( PeerDroidSample activity) {
       super(activity);
       this.activity = activity;
    }
	
	/**
	 * Method call when the Dialog Window start
	 */
    protected void onStart() {
    	
        super.onStart();
        setContentView(R.layout.settings);
        getWindow().setFlags(4, 4);
        setTitle("Connection Settings");
        
        //Gui elements taken from the layout file
        Button btnOk = (Button)findViewById(R.id.ok);
        serverText = (EditText)findViewById(R.id.host);
        
        //OkButton Listener
        btnOk.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
			   
				if( serverText.getText().toString().trim().length() > 0)
				{
					/*
					activity.getMPlay().setBackground(R.drawable.play);
					activity.getMPause().setBackground(R.drawable.pause);
					activity.getMStop().setBackground(R.drawable.stop);
					
					activity.getMyPlayer().changeMediaPath(serverText.getText().toString());
					activity.getInfoTextView().setText("Media Path: " + activity.getMyPlayer().getPath());
					activity.getSecTextView().setText("Sec: 0.0");
					*/
				}
				
				dismiss();
			}
        	});
    }
  
}
