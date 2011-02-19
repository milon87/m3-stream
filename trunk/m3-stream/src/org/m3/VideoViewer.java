package org.m3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.URLUtil;
import android.widget.MediaController;
import android.widget.Toast;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.m3.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.app.Activity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.Toast;



public class VideoViewer extends Activity {

    Button b;
    VideoView preview;
    SurfaceHolder holder;
    private MediaPlayer mp;
    //public String path2 = "http://daily3gp.com/vids/747.3gp";
    private VideoView mVideoView;
    private VideoView mVideoView2;
    private static final String TAG = "VideoViewDemo";

	private EditText mPath;
	private ImageButton mPlay;
	private ImageButton mPause;
	private ImageButton mReset;
	private ImageButton mStop;
	private String current;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_view);
    
        Button btnHome = (Button) findViewById(R.id.btnHome);
                
        btnHome.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View arg0) {
        		if(mp!=null) mp.stop();
            	finish();
			}
        });
        
  
        
        /*VideoView myVideoView = (VideoView)findViewById(R.id.surface_view);
 

        String viewSource ="rtsp://v5.cache1.c.youtube.com/CjYLENy73wIaLQklThqIVp_AsxMYESARFEIJbXYtZ29vZ2xlSARSBWluZGV4YIvJo6nmx9DvSww=/0/0/0/video.3gp";
        
        myVideoView.setVideoURI(Uri.parse(viewSource));
        myVideoView.setMediaController(new MediaController(this));
        myVideoView.requestFocus();
        myVideoView.start();*/

       /* VideoView videoView = (VideoView) findViewById(R.id.surface_view2);
		MediaController mediaController = new MediaController(this);
		mediaController.setAnchorView(videoView);
// Set video link (mp4 format )
		Uri video = Uri.parse("http://daily3gp.com/vids/747.3gp");
		videoView.setMediaController(mediaController);
		videoView.setVideoURI(video);
		videoView.start();*/
       
        
        mVideoView = (VideoView) findViewById(R.id.surface_view);
        //mVideoView2 = (VideoView)findViewById(R.id.surface_view2);
        
        mPath = (EditText) findViewById(R.id.path);
		mPath.setText("http://daily3gp.com/vids/747.3gp");
		//mPath.setText("http://daily3gp.com/vids/747.3gp");
		
		Button mPlay = (Button) findViewById(R.id.play);
		Button mPause = (Button) findViewById(R.id.pause);
		Button mReset = (Button) findViewById(R.id.reset);
		Button mStop = (Button) findViewById(R.id.stop);
		
		/*mPlay = (ImageButton) findViewById(R.id.play);
		mPause = (ImageButton) findViewById(R.id.pause);
		mReset = (ImageButton) findViewById(R.id.reset);
		mStop = (ImageButton) findViewById(R.id.stop);*/

		mPlay.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				playVideo();
			}
		});
		mPause.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (mVideoView != null) {
					mVideoView.pause();
				}
			}
		});
		mReset.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (mVideoView != null) {
					mVideoView.seekTo(0);
				}
			}
		});
		mStop.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (mVideoView != null) {
					current = null;
					mVideoView.stopPlayback();
				}
			}
		});
		runOnUiThread(new Runnable(){
			public void run() {
				playVideo();

			}

		});
	}

    private void playVideo() {
		try {
			final String path = mPath.getText().toString();
			Log.v(TAG, "path: " + path);
			if (path == null || path.length() == 0) {
				Toast.makeText(VideoViewer.this, "File URL/path is empty",
						Toast.LENGTH_LONG).show();

			} else {
				// If the path has not changed, just start the media player
				if (path.equals(current) && mVideoView != null) {
					mVideoView.start();
					mVideoView.requestFocus();
					return;
				}
				Toast.makeText(VideoViewer.this, path,
						Toast.LENGTH_LONG).show();
				current = path;
				Uri video = Uri.parse(path);
				mVideoView.setMediaController(new MediaController(this));
				mVideoView.setVideoURI(video);
				//mVideoView.setVideoPath(getDataSource(path));
				mVideoView.start();
				mVideoView.requestFocus();
            
			}
		} catch (Exception e) {
			Log.e(TAG, "error: " + e.getMessage(), e);
			if (mVideoView != null) {
				mVideoView.stopPlayback();
			}
		}
	}

	private String getDataSource(String path) throws IOException {
		if (!URLUtil.isNetworkUrl(path)) {
			return path;
		} else {
			URL url = new URL(path);
			URLConnection cn = url.openConnection();
			cn.connect();
			InputStream stream = cn.getInputStream();
			if (stream == null)
				throw new RuntimeException("stream is null");
			File temp = File.createTempFile("mediaplayertmp", "dat");
			temp.deleteOnExit();
			String tempPath = temp.getAbsolutePath();
			FileOutputStream out = new FileOutputStream(temp);
			byte buf[] = new byte[128];
			do {
				int numread = stream.read(buf);
				if (numread <= 0)
					break;
				out.write(buf, 0, numread);
			} while (true);
			try {
				stream.close();
			} catch (IOException ex) {
				Log.e(TAG, "error: " + ex.getMessage(), ex);
			}
			return tempPath;
		}
	}
}
