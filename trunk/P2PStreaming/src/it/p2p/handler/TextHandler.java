package it.p2p.handler;

import it.p2p.P2PStreaming;

import java.io.File;

import android.media.MediaPlayer;
import android.os.FileUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;
import android.widget.TextView;

/**
 * P2P Streaming Player for Google Android
 * 
 * Handler used to download and reproduce datas from streaming server.
 * 
 * @author Marco Picone
 * @created 01/09/2008
 */
public class TextHandler implements Runnable {

	private TextView secTextView;
	private MediaPlayer mp;
	
	public TextHandler( TextView secTextView, MediaPlayer mp ) {
		super();
		
		Log.d(P2PStreaming.TAG,"TextHandler COSTRUCTOR CALL !");
		
		this.secTextView = secTextView;
		this.mp = mp;
		
	}

	public void run() {

		Log.d(P2PStreaming.TAG,"TextHandler RUN CALL !");
	}
	
}
