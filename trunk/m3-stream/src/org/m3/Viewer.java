package org.m3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.URLUtil;
import android.widget.MediaController;
import android.widget.Toast;

import org.m3.R;
import org.m3.server.ServerService;
import org.m3.util.Utils;

import android.util.Log;
import android.widget.VideoView;


public class Viewer extends Activity {
    private VideoView mVideoView;
	private String currentURL;
	//private final String URL = "http://daily3gp.com/vids/747.3gp";	
	private static final String STREAM_FILE_NAME = "___v_video_streamed";
	private static final int COPY_CHUNK_SIZE =  4 << 10; // 4 kBytes
	private ServerService service;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewer);
    
        service = new ServerService(this);
        mVideoView = (VideoView) findViewById(R.id.surface_view);
        
		runOnUiThread(new Runnable(){
			public void run() {
				playVideo();

			}
		});
	}

    private void playVideo() {
		try {
			//final String path = URL;
			final String path = service.getVideoURI("video.vms");
			
			//InputStream stream = service.getStream("video.vms");
			
			Log.v("Viewer", "path: " + path);
			if(path == null || path.length() == 0) {
				Toast.makeText(Viewer.this, "File URL/path is empty",
						Toast.LENGTH_LONG).show();
			} else {
				// If the path has not changed, just start the media player
				if (path.equals(currentURL) && mVideoView != null) {
					mVideoView.start();
					mVideoView.requestFocus();
					return;
				}
				Toast.makeText(Viewer.this, path,
						Toast.LENGTH_LONG).show();
				currentURL = path;
				//Uri video = Uri.parse(path);
				MediaController mc = new MediaController(this); 
				
				/*HttpParams httpParameters = new BasicHttpParams();
				// Set the timeout in milliseconds until a connection is established.
				int timeoutConnection = 10000;
				HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				// Set the default socket timeout (SO_TIMEOUT) 
				// in milliseconds which is the timeout for waiting for data.
				int timeoutSocket = 10000;
				HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
				*/
				
				mVideoView.setMediaController(mc);
				//mVideoView.setVideoURI(video);
				mVideoView.setVideoPath(getDataSource(path));
				mVideoView.start();
				mVideoView.requestFocus();
            }
		} catch(Exception e) {
			Log.e("Viewer", "error: " + e.getMessage(), e);
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
			final InputStream stream = cn.getInputStream();
			if(stream == null)
				throw new RuntimeException("stream is null");
			final File streamFile = File.createTempFile(STREAM_FILE_NAME, "dat", Utils.getDefaultCacheDir(this));
			streamFile.deleteOnExit();
			String tempPath = streamFile.getAbsolutePath();
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						FileOutputStream out = new FileOutputStream(streamFile);
						byte buf[] = new byte[COPY_CHUNK_SIZE];
						do {
							int numread = stream.read(buf);
							if (numread <= 0)
								break;
							//buf = Base64.decode(buf, Base64.DEFAULT);
							out.write(buf, 0, numread); //buf.length
						} while (true);
						stream.close();
					} catch (IOException ex) {
						Log.e("Viewer", "error: " + ex.getMessage(), ex);
					}
				}
			}).start();
			return tempPath;
		}
	}
	
}
