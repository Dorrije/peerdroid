package it.p2p.gui;

import java.io.IOException;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.TextView;

/**
 * P2P Streaming Player for Google Android
 * 
 * Class that extends Android MediaPlayer to add new funtions and simplify the
 * use of Player.
 * 
 * @author Marco Picone
 * @created 01/09/2008
 */
public class MyMediaPlayer extends MediaPlayer {

	// Variable used to identify the class output in the Android LogConsole
	private String TAG = "MyMediaPlayer";

	private String path;
	private SurfaceHolder holder;
	private int audio_type;
	private boolean init = false;
	private PlayerHorizontalSlider playerBar;
	private TextView secTextView;
	private final Handler handler = new Handler();

	/**
	 * Class constructor
	 * 
	 * @param path
	 * @param holder
	 * @param audio_type
	 */
	public MyMediaPlayer(String path, SurfaceHolder holder, int audio_type) {
		super();

		Log.d(TAG, "MyMediaPlayer Costructor CALL !");

		this.path = path;
		this.holder = holder;
		this.audio_type = audio_type;

	}

	/**
	 * Method used to initialize and create the MediaPlayer. It sets the
	 * streaming path,android holder,audio type and starts prepare method.
	 */
	public void init() {

		Log.d(TAG, "MyMediaPlayer init CALL !");

		init = true;

		try {

			setDataSource(path);
			setDisplay(holder);
			prepare();

			setAudioStreamType(audio_type);

			if (playerBar != null) {
				playerBar.setMax(getDuration());
				playerBar.setMp(this);
			}

		} catch (IllegalArgumentException e) {
			Log.e(TAG, "MyMediaPlayer EXCEPTION " + e);
		} catch (IOException e) {
			Log.e(TAG, "MyMediaPlayer IO EXCEPTION " + e);
			e.printStackTrace();

		}
	}

	/**
	 * Method used to play the MediaStream
	 */
	public void playMedia() {
		
		Log.d(TAG, "MyMediaPlayer playMedia CALL !");

		//Starts the MediaPlayer
		start();

		final long mStartTime = SystemClock.uptimeMillis();

		//Creates a new thread to manage the TextView about Seconds and to manage the ProgressBar
		Runnable updater = new Runnable() {
			public void run() {

				final long start = mStartTime;
				long millis = SystemClock.uptimeMillis() - start;

				if (secTextView != null)
					secTextView.setText("Secs: "
							+ (double) (getCurrentPosition() / 1000.0));

				if (playerBar != null)
					playerBar.setProgress(getCurrentPosition());

				if (isPlaying() && (playerBar != null || secTextView != null))
					handler.postAtTime(this, start + millis + 300);

			}
		};

		if (playerBar != null || secTextView != null)
			handler.post(updater);

	}

	/**
	 * Method used to play the MediaStream
	 */
	public void stopMedia() {
		Log.d(TAG, "MyMediaPlayer stopMedia CALL !");
		seekTo(0);
		pause();
	}

	/**
	 * Method used to pause the MediaStream
	 */
	public void pauseMedia() {
		Log.d(TAG, "MyMediaPlayer pauseMedia CALL !");
		pause();

	}

	/**
	 * Method used to change the video size of the MediaStream
	 */
	public void changeVideoSize() {
		Log.d(TAG, "MyMediaPlayer changeVideoSize CALL !");
	}

	/**
	 * Method used to path of the MediaStream
	 */
	public void changeMediaPath(String new_path) {
		Log.d(TAG, "MyMediaPlayer changeMediaPath CALL !");

		path = new_path;
		reset();
		init();

	}

	public boolean isInit() {
		return init;
	}

	public PlayerHorizontalSlider getPlayerBar() {
		return playerBar;
	}

	public void setPlayerBar(PlayerHorizontalSlider playerBar) {
		this.playerBar = playerBar;
	}

	public TextView getSecTextView() {
		return secTextView;
	}

	public void setSecTextView(TextView secTextView) {
		this.secTextView = secTextView;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
