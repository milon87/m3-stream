package org.m3.view.tabs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.m3.model.Video;
import org.m3.server.ServerService;
import org.m3.util.Utils;
import org.m3.xml.DataHandler;

import com.flazr.rtmp.client.RtmpClientPipelineFactory;
import com.flazr.rtmp.client.RtmpClientSession;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.LinearLayout.LayoutParams;


public class VideoActivity extends Activity {
    private Video video;
	private VideoView videoView;
	private ProgressBar progress;
	private int progressStatus;
	private Handler pHandler = new Handler();
	private TableRow mainRow1;
	
	private ServerService service;
	private String currentURL;
	private final String URL = "rtmp://172.26.24.10/oflaDemo";	
	private static final String STREAM_FILE_NAME = "___v_video_streamed";
	private static final int COPY_CHUNK_SIZE =  4 << 10; // 4 kBytes
	
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int categoryId = this.getIntent().getExtras().getInt("categoryId");
        final int videoId = this.getIntent().getExtras().getInt("videoId");
        
        video = DataHandler.getCategories().get(categoryId).getVideos().get(videoId);
        
        LinearLayout main = new LinearLayout(this);
        main.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
        main.setOrientation(LinearLayout.VERTICAL);
        
        TableLayout mainPanel = new TableLayout(this);
        mainPanel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        mainPanel.setPadding(5, 20, 5, 10);
        mainPanel.setStretchAllColumns(true);
        mainRow1 = new TableRow(this);
    
        drawMainRow1();
        
        mainPanel.addView(mainRow1);
        main.addView(mainPanel);
        
        progress = new ProgressBar(this);
	    progress.setLayoutParams(new LayoutParams(40, 40));
	    progress.setMax(10);
        // Start lengthy operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                while(progressStatus < 10) {
                    doCheck();
                    pHandler.post(new Runnable() {
                        public void run() {
                            progress.setProgress(progressStatus);
                        }
                    });
                }
            }
        }).start();
        main.addView(progress);
        
        setContentView(main);
    }
	
    private void drawMainRow1() {
    	mainRow1.removeAllViews();
    	
    	videoView = new VideoView(this);
    	videoView.setPadding(10,10,10,10);
    	service = new ServerService(this);
        
		runOnUiThread(new Runnable(){
			public void run() {
				playVideo();
			}
		});

        
        mainRow1.addView(videoView);
    }
    
 	private void doCheck() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
    
    private void playVideo() {
		try {
			   RtmpClientSession session = new RtmpClientSession("rtmp://172.26.24.10:1935/oflaDemo/stream1301398793883");
			   connect(session);
			
			/*final String path = URL;
			//final String path = service.getVideoURI("video.vms"); // video.getUrl();
			//InputStream stream = service.getStream("video.vms");
			
			Log.v("Viewer", "path: " + path);
			if(path == null || path.length() == 0) {
				Toast.makeText(VideoActivity.this, "File URL/path is empty",
						Toast.LENGTH_LONG).show();
			} else {
				// If the path has not changed, just start the media player
				if (path.equals(currentURL) && videoView != null) {
					videoView.start();
					videoView.requestFocus();
					return;
				}
				Toast.makeText(VideoActivity.this, path,
						Toast.LENGTH_LONG).show();
				currentURL = path;
				//Uri video = Uri.parse(path);
				MediaController mc = new MediaController(this); 
				
				//HttpParams httpParameters = new BasicHttpParams();
				// Set the timeout in milliseconds until a connection is established.
				//int timeoutConnection = 10000;
				//HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				// Set the default socket timeout (SO_TIMEOUT) 
				// in milliseconds which is the timeout for waiting for data.
				//int timeoutSocket = 10000;
				//HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
				
				
				videoView.setMediaController(mc);
				//mVideoView.setVideoURI(video);
				videoView.setVideoPath(getDataSource(path));
				videoView.start();
				videoView.requestFocus();
            }*/
		} catch(Exception e) {
			Log.e("Viewer", "error: " + e.getMessage(), e);
			if(videoView != null) {
				videoView.stopPlayback();
			}
		}
	}

 	public static void connect(RtmpClientSession session) {
	    ChannelFactory factory = new NioClientSocketChannelFactory (
	        Executors.newCachedThreadPool(),
	        Executors.newCachedThreadPool());
	    ClientBootstrap bootstrap = new ClientBootstrap(factory);
	    bootstrap.setPipelineFactory(new RtmpClientPipelineFactory(session));
	    bootstrap.setOption("tcpNoDelay" , true);
	    bootstrap.setOption("keepAlive", true);
	    ChannelFuture future = bootstrap.connect(new InetSocketAddress(session.getHost(), session.getPort()));
	    future.awaitUninterruptibly();
	    if(!future.isSuccess()) {
	        future.getCause().printStackTrace();
	    }
	    future.getChannel().getCloseFuture().awaitUninterruptibly();
	    factory.releaseExternalResources();
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
