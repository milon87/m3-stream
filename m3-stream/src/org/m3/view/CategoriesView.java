package org.m3.view;

import org.m3.R;
import org.m3.view.adapters.CategoriesAdapter;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class CategoriesView extends Activity implements
				OnBufferingUpdateListener, OnCompletionListener,
				OnPreparedListener, OnVideoSizeChangedListener, SurfaceHolder.Callback {
	 private static final String TAG = "MediaPlayerDemo";
	    private int mVideoWidth;
	    private int mVideoHeight;
	    private MediaPlayer mMediaPlayer;
	    private SurfaceView mPreview;
	    private SurfaceHolder holder;
	    private String path;
	    private static final int LOCAL_VIDEO = 4;
	    private static final int STREAM_VIDEO = 5;
	    private boolean mIsVideoSizeKnown = false;
	    private boolean mIsVideoReadyToBePlayed = false;

	    /**
	     * 
	     * Called when the activity is first created.
	     */
	    @Override
	    public void onCreate(Bundle icicle) {
	        super.onCreate(icicle);
	        setContentView(R.layout.categories);
	        mPreview = (SurfaceView) findViewById(R.id.surface);
	        holder = mPreview.getHolder();
	        holder.addCallback(this);
	        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	        
	        //playVideo(STREAM_VIDEO);
	        
	        final Button btnBroadcast = (Button) findViewById(R.id.btnBroadcast);
	        btnBroadcast.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
	    	        Intent intent = new Intent(CategoriesView.this, BroadcastDetailsView.class);
	    	        startActivityForResult(intent, 0);
				}
	        });

	        final GridView gridview = (GridView) findViewById(R.id.gridCategories);
	        gridview.setAdapter(new CategoriesAdapter(this, ""));
	        gridview.getBackground().setDither(true);

	        gridview.setOnItemClickListener(new OnItemClickListener() {
	        	@Override
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	    	        Intent intent = new Intent(CategoriesView.this, VideosView.class);
	    	        intent.putExtra("categoryId", position);
	    	        startActivityForResult(intent, 0);
	            }
	        });

	    
	        final EditText edtSearch = (EditText) findViewById(R.id.edtSearch);
	        
	        final ImageView btnSearch = (ImageView) findViewById(R.id.btnSearch);
	        btnSearch.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					gridview.setAdapter(new CategoriesAdapter(CategoriesView.this, edtSearch.getText().toString()));
				}
	        });

	    }

	    private void playVideo(Integer Media) {
	        doCleanUp();
	        try {

	            switch (Media) {
	                case LOCAL_VIDEO:
	                    /*
	                     * TODO: Set the path variable to a local media file path.
	                     */
	                    path = "";
	                    if (path == "") {
	                        // Tell the user to provide a media file URL.
	                        Toast
	                                .makeText(
	                                		CategoriesView.this,
	                                        "Please edit MediaPlayerDemo_Video Activity, "
	                                                + "and set the path variable to your media file path."
	                                                + " Your media file must be stored on sdcard.",
	                                        Toast.LENGTH_LONG).show();

	                    }
	                    break;
	                case STREAM_VIDEO:
	                    /*
	                     * TODO: Set path variable to progressive streamable mp4 or
	                     * 3gpp format URL. Http protocol should be used.
	                     * Mediaplayer can only play "progressive streamable
	                     * contents" which basically means: 1. the movie atom has to
	                     * precede all the media data atoms. 2. The clip has to be
	                     * reasonably interleaved.
	                     * 
	                     */
	                    if (path == "") {
	                        // Tell the user to provide a media file URL.
	                        Toast
	                                .makeText(
	                                		CategoriesView.this,
	                                        "Please edit MediaPlayerDemo_Video Activity,"
	                                                + " and set the path variable to your media file URL.",
	                                        Toast.LENGTH_LONG).show();

	                    }

	                    break;


	            }

	            // Create a new media player and set the listeners
	            mMediaPlayer = new MediaPlayer();
	            mMediaPlayer.setDataSource(path);
	            mMediaPlayer.setDisplay(holder);
	            mMediaPlayer.prepare();
	            mMediaPlayer.setOnBufferingUpdateListener(this);
	            mMediaPlayer.setOnCompletionListener(this);
	            mMediaPlayer.setOnPreparedListener(this);
	            mMediaPlayer.setOnVideoSizeChangedListener(this);
	            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


	        } catch (Exception e) {
	            Log.e(TAG, "error: " + e.getMessage(), e);
	        }
	    }

	    public void onBufferingUpdate(MediaPlayer arg0, int percent) {
	        Log.d(TAG, "onBufferingUpdate percent:" + percent);

	    }

	    public void onCompletion(MediaPlayer arg0) {
	        Log.d(TAG, "onCompletion called");
	    }

	    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
	        Log.v(TAG, "onVideoSizeChanged called");
	        if (width == 0 || height == 0) {
	            Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
	            return;
	        }
	        mIsVideoSizeKnown = true;
	        mVideoWidth = width;
	        mVideoHeight = height;
	        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
	            startVideoPlayback();
	        }
	    }

	    public void onPrepared(MediaPlayer mediaplayer) {
	        Log.d(TAG, "onPrepared called");
	        mIsVideoReadyToBePlayed = true;
	        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
	            startVideoPlayback();
	        }
	    }

	    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
	        Log.d(TAG, "surfaceChanged called");

	    }

	    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
	        Log.d(TAG, "surfaceDestroyed called");
	    }


	    public void surfaceCreated(SurfaceHolder holder) {
	        Log.d(TAG, "surfaceCreated called");
	        
	        path = "http://commonsware.com/misc/test2.3gp";
            playVideo(STREAM_VIDEO);

	    }

	    @Override
	    protected void onPause() {
	        super.onPause();
	        releaseMediaPlayer();
	        doCleanUp();
	    }

	    @Override
	    protected void onDestroy() {
	        super.onDestroy();
	        releaseMediaPlayer();
	        doCleanUp();
	    }

	    private void releaseMediaPlayer() {
	        if (mMediaPlayer != null) {
	            mMediaPlayer.release();
	            mMediaPlayer = null;
	        }
	    }

	    private void doCleanUp() {
	        mVideoWidth = 0;
	        mVideoHeight = 0;
	        mIsVideoReadyToBePlayed = false;
	        mIsVideoSizeKnown = false;
	    }

	    private void startVideoPlayback() {
	        Log.v(TAG, "startVideoPlayback");
	        holder.setFixedSize(mVideoWidth, mVideoHeight);
	        mMediaPlayer.start();
	    }
	    
}
	
	/** Called when the activity is first created. */
    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories);

        videoView = (VideoView) findViewById(R.id.videoView);
		mc = new MediaController(this);
		videoView.setMediaController(mc);
        videoView.setOnErrorListener(this);

        play();
        
        final Button btnBroadcast = (Button) findViewById(R.id.btnBroadcast);
        btnBroadcast.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
    	        Intent intent = new Intent(CategoriesView.this, BroadcastDetailsView.class);
    	        startActivityForResult(intent, 0);
			}
        });

        final GridView gridview = (GridView) findViewById(R.id.gridCategories);
        gridview.setAdapter(new CategoriesAdapter(this, ""));
        gridview.getBackground().setDither(true);

        gridview.setOnItemClickListener(new OnItemClickListener() {
        	@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    	        Intent intent = new Intent(CategoriesView.this, VideosView.class);
    	        intent.putExtra("categoryId", position);
    	        startActivityForResult(intent, 0);
            }
        });

    
        final EditText edtSearch = (EditText) findViewById(R.id.edtSearch);
        
        final ImageView btnSearch = (ImageView) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				gridview.setAdapter(new CategoriesAdapter(CategoriesView.this, edtSearch.getText().toString()));
			}
        });
    }

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		play();
		return false;
	}
	
	private void play() {
        String path1="http://commonsware.com/misc/test2.3gp";
        Uri uri=Uri.parse(path1);
  	    videoView.setVideoURI(uri);
  	    videoView.prepare();
		videoView.start();
	}

}*/
