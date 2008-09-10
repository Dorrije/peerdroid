package it.p2p.gui;

import java.util.Map;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

public class PlayerHorizontalSlider extends ProgressBar {

	private OnProgressChangeListener listener;
	private final Handler handler = new Handler();
	private MediaPlayer mp;
	public int my_progress;
	
	private static int padding = 2;

	public interface OnProgressChangeListener {
		void onProgressChanged(View v, int progress);
	}
	
	public PlayerHorizontalSlider(Context context, AttributeSet attrs,
			Map inflateParams, int defStyle) {
		super(context, attrs, defStyle);
	}

	public PlayerHorizontalSlider(Context context, AttributeSet attrs,
			Map inflateParams) {
		super(context, attrs, android.R.attr.progressBarStyleHorizontal);

	}

	public PlayerHorizontalSlider(Context context) {
		super(context);

	}

	public void setOnProgressChangeListener(OnProgressChangeListener l) {
		listener = l;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();

		if (action == MotionEvent.ACTION_DOWN
				|| action == MotionEvent.ACTION_MOVE) {
			float x_mouse = event.getX() - padding;
			float width = getWidth() - 2*padding;
			my_progress = Math.round((float) getMax() * (x_mouse / width));

			if (my_progress < 0)
				my_progress = 0;
			
			this.setProgress(my_progress);
			
			if (listener != null)
				listener.onProgressChanged(this, my_progress);

			Runnable updater = new Runnable() {
				public void run() {
						if(mp != null)
						{
							if(!mp.isPlaying())
							{
								mp.seekTo(my_progress);
								mp.pause();
							}
							else
								mp.seekTo(my_progress);
								
						}	
					}
			};

			handler.post(updater);
			
		}

		return true;
	}

	public MediaPlayer getMp() {
		return mp;
	}

	public void setMp(MediaPlayer mp) {
		this.mp = mp;
		mp.seekTo(my_progress);
	}
}
