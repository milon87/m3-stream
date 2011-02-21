package org.m3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.URLUtil;
import android.widget.MediaController;
import android.widget.Toast;
import org.m3.R;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Button;
import android.widget.EditText;
import android.widget.VideoView;


public class Viewer extends Activity {
    Button b;
    VideoView preview;
    SurfaceHolder holder;
    private VideoView mVideoView;
    private static final String TAG = "VideoViewDemo";
	private String current;
	String URL = "http://daily3gp.com/vids/747.3gp";	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewer);
    
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
		
		runOnUiThread(new Runnable(){
			public void run() {
				playVideo();

			}

		});
	}

    private void playVideo() {
		try {
			final String path = URL;
			Log.v(TAG, "path: " + path);
			if(path == null || path.length() == 0) {
				Toast.makeText(Viewer.this, "File URL/path is empty",
						Toast.LENGTH_LONG).show();
			} else {
				// If the path has not changed, just start the media player
				if (path.equals(current) && mVideoView != null) {
					mVideoView.start();
					mVideoView.requestFocus();
					return;
				}
				Toast.makeText(Viewer.this, path,
						Toast.LENGTH_LONG).show();
				current = path;
				Uri video = Uri.parse(path);
				mVideoView.setMediaController(new MediaController(this));
				mVideoView.setVideoURI(video);
				//mVideoView.setVideoPath(getDataSource(path));
				mVideoView.start();
				mVideoView.requestFocus();
            }
		} catch(Exception e) {
			Log.e(TAG, "error: " + e.getMessage(), e);
			if(mVideoView != null) {
				mVideoView.stopPlayback();
			}
		}
	}

	private String getDataSource(String path) throws IOException {
		if(!URLUtil.isNetworkUrl(path)) {
			return path;
		} else {
			URL url = new URL(path);
			URLConnection cn = url.openConnection();
			cn.connect();
			InputStream stream = cn.getInputStream();
			if(stream == null)
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
