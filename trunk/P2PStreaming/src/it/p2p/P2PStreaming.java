package it.p2p;

import it.p2p.dialog.GraphDialog;
import it.p2p.dialog.IntroDialog;
import it.p2p.dialog.NetworkInfoDialog;
import it.p2p.dialog.SettingsDialog;
import it.p2p.gui.MyMediaPlayer;
import it.p2p.handler.StreamingHandler;
import it.p2p.info.NetworkInformation;
import it.p2p.service.JXTAService;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 *  P2P Streaming Player for Google Android
 *  
 *  Main Activity that creates the main GUI of the program.
 *
 * @author   Marco Picone 
 * @created  01/09/2008
 */
public class P2PStreaming extends Activity implements Callback {

	//Variable used to identify the class output in the Android LogConsole
	public static String TAG = "P2P Streaming Player";

	//GUI Variabiles
	private SurfaceView mPreview;
	private ImageButton mPlay;
	private ImageButton mPause;
	private ImageButton mStop;
	private ImageButton eject;
	private ProgressBar progressBar;
	private TextView secTextView;
	private TextView infoTextView;
	private SurfaceHolder holder;
	private MyMediaPlayer myPlayer;
	private Handler handler = new Handler();
	private Thread downloadThread;
	
	//Dialod windows variables
	private SettingsDialog mDialog;
	private GraphDialog dialogGraph;
	private NetworkInfoDialog infoDialog;
	private IntroDialog introDialog;
	
	//NetworkInformation object
	private NetworkInformation networkInfo = new NetworkInformation();
	
	//Contains the Server Path
	private String path = "http://pkweb.altervista.org/video_mp4.mp4";
	
	//JXTA Variables
	private String myPeerName = "AndroidPeer";
	private JXTAService managerJXTA;

	//Listener for Eject Button
	private View.OnClickListener ejectButton_OnCliclistener = new View.OnClickListener() {
		public void onClick(View view) {

			handler.post(new Runnable() {
				public void run() {
					mDialog.show();
				}
			});
		}
	};

	//Listener for Play Button
	private View.OnClickListener playButton_OnCliclistener = new View.OnClickListener() {
		public void onClick(View view) {

			mPlay.setBackgroundResource(R.drawable.play_over);
			mPause.setBackgroundResource(R.drawable.pause);
			mStop.setBackgroundResource(R.drawable.stop);

			if(myPlayer.isInit())
			{				
				myPlayer.playMedia();
			}
			else{
				//New thread for the streaming manager
				downloadThread = new Thread(new StreamingHandler(myPlayer,networkInfo), "DownloadIncremental Thread");
				downloadThread.start();
			}

		}
	};

	//Listener for Pause Button
	private View.OnClickListener pauseButton_OnCliclistener = new View.OnClickListener() {
		public void onClick(View view) {

			mPlay.setBackgroundResource(R.drawable.play);
			mStop.setBackgroundResource(R.drawable.stop);
			mPause.setBackgroundResource(R.drawable.pause_over);

			if(myPlayer.isInit())
				myPlayer.pauseMedia();
		}
	}; 

	//Listener for Stop Button
	private View.OnClickListener stopButton_OnCliclistener = new View.OnClickListener() {
		public void onClick(View view) {

			mPlay.setBackgroundResource(R.drawable.play);
			mPause.setBackgroundResource(R.drawable.pause);
			mStop.setBackgroundResource(R.drawable.stop_over);

			if(myPlayer.isInit())
				myPlayer.stopMedia();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		setTitle("Android P2P Streaming");
		
		mPreview = (SurfaceView) findViewById(R.id.videoView);
		mPlay = (ImageButton) findViewById(R.id.playButton);
		mPause = (ImageButton) findViewById(R.id.pauseButton);
		mStop = (ImageButton) findViewById(R.id.stopButton);
		eject = (ImageButton) findViewById(R.id.ejectButton);
		progressBar = (ProgressBar) findViewById(R.id.progress_bar);
		secTextView = (TextView) findViewById(R.id.secTextView);
		infoTextView = (TextView) findViewById(R.id.infoTextView);

		infoTextView.setText("Media Path: " + path);
		secTextView.setText("Sec: 0.0");

		//New Objects for the Dialog Windows
		mDialog = new SettingsDialog(this);
		infoDialog = new NetworkInfoDialog(this);
		dialogGraph = new GraphDialog(this);
		
		//Initialize and start the Introduction Dialog Window, that initialize and start the JXTA System
		introDialog = new IntroDialog(this, managerJXTA, getFileStreamPath("jxta"), myPeerName);
		introDialog.show();
		
		//Set buttons listeners
		eject.setOnClickListener(ejectButton_OnCliclistener);
		mPlay.setOnClickListener(playButton_OnCliclistener);
		mPause.setOnClickListener(pauseButton_OnCliclistener);
		mStop.setOnClickListener(stopButton_OnCliclistener);

		// Set the transparency
		getWindow().setFormat(PixelFormat.RGBX_8888);

		holder = mPreview.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.setFixedSize(480, 300);

		//Create the MediaPlayer
		myPlayer = new MyMediaPlayer(path,holder,2);

		//Set TextView that shows progress of seconds
		myPlayer.setSecTextView(secTextView);

	}

	@Override
	/**
	 * Called on activity destroyed
	 */
	protected void onDestroy() {
		super.onDestroy();

		Log.d(TAG,"onDestroy CALL !");

		downloadThread.interrupt();

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
		}
		return false;

	}


	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
	}
}