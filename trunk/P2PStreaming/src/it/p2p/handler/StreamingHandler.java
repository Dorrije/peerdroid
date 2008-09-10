package it.p2p.handler;

import it.p2p.P2PStreaming;
import it.p2p.gui.MyMediaPlayer;
import it.p2p.info.NetworkInformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.FileUtils;
import android.os.Handler;
import android.util.Log;

/**
 * P2P Streaming Player for Google Android
 * 
 * Handler used to download and reproduce datas from streaming server.
 * 
 * @author Marco Picone
 * @created 01/09/2008
 */
public class StreamingHandler implements Runnable {

	private int totalKbRead;

	// After this limit the player start to reproduce the stream
	private double KB_LIMIT = 500;

	// Streaming Server Path
	private String path;

	// MediaPlayer used to reproduce the stream
	private MyMediaPlayer mp;

	// NetworkInformation object used to store data about download and upload
	// speeds
	private NetworkInformation networkInfo;

	// File used to store datas during the download process. File are created on
	// sdcard
	private File downloadingMediaFile;
	private File bufferedFile;

	private final Handler handler = new Handler();

	/**
	 * Class constructor, creates files and sets importants variables
	 * 
	 * @param mp_
	 * @param networkInfo
	 */
	public StreamingHandler(MyMediaPlayer mp_, NetworkInformation networkInfo) {
		super();

		Log.d(P2PStreaming.TAG, "DownloadIncrementalHandler COSTRUCTOR CALL !");

		try {
			downloadingMediaFile = new File("/sdcard/downloadingMedia.dat");

			if (downloadingMediaFile.exists()) {
				downloadingMediaFile.delete();
				downloadingMediaFile.createNewFile();
			} else
				downloadingMediaFile.createNewFile();

			bufferedFile = new File("/sdcard/playingMedia.dat");

			if (bufferedFile.exists()) {
				bufferedFile.delete();
				bufferedFile.createNewFile();
			} else
				bufferedFile.createNewFile();

		} catch (Exception e) {
			Log.e(P2PStreaming.TAG, "StreamingHandler COSTRUCTOR EXCEPTION : ",
					e);
		}

		this.mp = mp_;
		this.path = mp.getPath();
		this.networkInfo = networkInfo;

	}

	/**
	 * Thread run method
	 */
	public void run() {
		Log.d(P2PStreaming.TAG, "DownloadIncrementalHandler RUN CALL !");

		// TODO Verificare che il file non sia giˆ stato scaricato e quindi lo
		// posso mandare in riproduzione !

		// Starts stream download
		downloadIncremental();

	}

	/**
	 * Method used to start the incremental download of the stream
	 */
	private void downloadIncremental() {

		Log.d(P2PStreaming.TAG, "downloadIncremental CALL ! : " + mp.getPath());

		try {

			// downloadingMediaFile = File.createTempFile("downloadingMedia",
			// ".dat");
			// bufferedFile = File.createTempFile("playingMedia", ".dat");

			// Opens the Connection with server
			URLConnection cn = new URL(path).openConnection();
			cn.connect();

			InputStream stream = cn.getInputStream();

			if (stream == null) {
				Log.e(P2PStreaming.TAG,
						"Unable to create InputStream for path:" + path);
			}

			FileOutputStream out = new FileOutputStream(downloadingMediaFile);

			int numread = 0;
			byte buf[] = new byte[16384];
			int totalBytesRead = 0, incrementalBytesRead = 0, segmentRead = 0;
			long start = 0;
			long finish = 0;
			long elapsed;
			do {

				start = System.currentTimeMillis();
				numread = stream.read(buf);
				finish = System.currentTimeMillis();

				if (numread <= 0)
					break;

				// Writes datas to file
				out.write(buf, 0, numread);

				totalBytesRead += numread;
				segmentRead += numread;
				incrementalBytesRead += numread;
				totalKbRead = totalBytesRead / 1000;

				double local_read = (double) (numread / 1000.0);
				elapsed = finish - start;
				double time = (elapsed / 1000.0);

				if (elapsed != 0) {
					double info_value = local_read / time;
					networkInfo.addDonwloadInfo(info_value);
					// Log.d(P2PStreaming.TAG,"Ho letto Kb : " + local_read + "
					// Total Kb: " + totalKbRead + " Time: " + time +" Value : "
					// + info_value);
				}

				// If the downloaded datas are enough, the MediaPlayer starts to
				// reproduce the stream
				if ((double) (segmentRead / 1000.0) >= KB_LIMIT) {

					Log.d(P2PStreaming.TAG, "Ho Superato la soglia minima: "
							+ (double) (segmentRead / 1000.0) + " Kbs");
					segmentRead = 0;

					// Creates a new thread to start to reproduce the partial
					// stream
					Runnable updater = new Runnable() {
						public void run() {
							playVideo();
						}
					};
					handler.post(updater);

				}

			} while (numread > 0);

			stream.close();
			Log.d(P2PStreaming.TAG,
					"Sono uscito dal ciclo di lettura, ho letto in totale: "
							+ totalKbRead);

			// Creates a new thread to start to reproduce the complete stream
			Runnable updater = new Runnable() {
				public void run() {
					playFullVideo();
				}
			};
			handler.post(updater);

		} catch (Exception e) {
			Log.e(P2PStreaming.TAG, "downloadIncremental EXCEPTION : ", e);
		}

	}

	/**
	 * Method used to start the MediaPlayer when a segment of stream was downloaded
	 */
	private void playVideo() {

		Log.d(P2PStreaming.TAG, "playVideo CALL !");

		//If the MediaPlayer is not initialized
		if (!mp.isInit()) {
			
			//Copy data from downloadFile to bufferFile used to reproduce the stream
			FileUtils.copyFile(downloadingMediaFile, bufferedFile);

			Log.d(P2PStreaming.TAG, "CREO IL NUOVO MEDIA PLAYER ! : "
					+ bufferedFile.getAbsolutePath());

			//create and Initialize MediaPlayer 
			mp.changeMediaPath(bufferedFile.getAbsolutePath());
			mp.playMedia();

		} else { //If the MediaPlayer is already initialized
			
			if (mp.isPlaying()) {
				Log.d(P2PStreaming.TAG, "Il MP e' in riproduzione ! DIFF: "
						+ (mp.getDuration() - mp.getCurrentPosition()));

				if ((mp.getDuration() - mp.getCurrentPosition()) <= 1) {
					int currentPosition = mp.getCurrentPosition();
					mp.pause();
					FileUtils.copyFile(downloadingMediaFile, bufferedFile);

					Log.d(P2PStreaming.TAG, "CREO IL NUOVO MEDIA PLAYER !");

					mp.changeMediaPath(bufferedFile.getAbsolutePath());
					mp.seekTo(currentPosition);
					mp.playMedia();
				}

			} else {
				Log.d(P2PStreaming.TAG, "Il MP non e' in riproduzione !");
				mp.playMedia();
			}
		}

	}

	/**
	 * Method used to start the MediaPlayer when the complete stream was downloaded
	 */
	private void playFullVideo() {

		Log.d(P2PStreaming.TAG, "playFullyVideo CALL !");

		int currentPosition = mp.getCurrentPosition();

		mp.pause();
		
		//Copy data from downloadFile to bufferFile used to reproduce the complete stream
		FileUtils.copyFile(downloadingMediaFile, bufferedFile);
		
		//Removes download file
		downloadingMediaFile.delete();
		
		Log.d(P2PStreaming.TAG,
				"CREO IL NUOVO MEDIA PLAYER PER RIPRODURRE IL VIDEO FINALE !");

		//create and Initialize MediaPlayer 
		mp.changeMediaPath(bufferedFile.getAbsolutePath());
		
		//Set the correct position of MediaPlayer
		mp.seekTo(currentPosition);
		
		//Starts the media stream
		mp.playMedia();

	}

	public double getKB_LIMIT() {
		return KB_LIMIT;
	}

	public void setKB_LIMIT(int kb_limit) {
		KB_LIMIT = kb_limit;
	}
}
