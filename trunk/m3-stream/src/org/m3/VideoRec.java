package org.m3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Arrays;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
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
import android.media.MediaRecorder;
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



public class VideoRec extends Activity {

    Button b;
    VideoView preview;
    SurfaceHolder holder;
    private MediaPlayer mp;
    //public String path2 = "http://daily3gp.com/vids/747.3gp";
    private VideoView mVideoView;
    private VideoView mVideoView2;
    private static final String TAG = "VideoViewDemo";

	private ImageButton mRec;
	private ImageButton mStop;

	
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
     
        String hostname = "127.0.0.1";
        int port = 1234;

        Socket socket = null;
		try {
			socket = new Socket(InetAddress.getByName(hostname), port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(socket);

        MediaRecorder recorder = new MediaRecorder();

        // Additional MediaRecorder setup (output format ... etc.) omitted

        recorder.setOutputFile(pfd.getFileDescriptor());

        try {
			recorder.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        recorder.start();
    }
}
