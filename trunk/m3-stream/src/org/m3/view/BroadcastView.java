package org.m3.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;

import android.view.View;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import org.m3.R;
import org.m3.Recorder;
import org.m3.Settings;
import org.m3.server.ServerService;
import org.m3.util.Utils;


public class BroadcastView extends Activity implements SurfaceHolder.Callback,
        View.OnClickListener, Camera.PreviewCallback 
        /*Camera.PictureCallback, Camera.AutoFocusCallback*/ {

    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private SurfaceView preview;
    private Recorder recorder;
    
    private Button btnStart;
    private Boolean isRecording = false;

    private static final int IDM_PREF = 101;
    
    private int videoBitrate;
    private int videoFramerate;
    private int audioBitrate;
    private int audioSamplingrate;
    private int audioChannels;
    private int videoWidth;
    private int videoHeight;
    private int videoMaxDuration;
    private int videoMaxFileSize;

    //private final String FILE_PATH = "/sdcard/test.mp4";/*String.format("/sdcard/%d.mp4", System.currentTimeMillis())*/
    private ServerService service;
    private String FILE_NAME;
    private String SERVER_IP;
	private static final int COPY_CHUNK_SIZE =  4 << 10; // 4 kBytes

	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.broadcast);
        service = new ServerService(this);
        
        preview = (SurfaceView) findViewById(R.id.SurfaceView01);

        surfaceHolder = preview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setText(this.getResources().getString(R.string.start));
        btnStart.setOnClickListener(this);

        recorder = new Recorder();
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        videoBitrate = Integer.parseInt(prefs.getString(getString(R.string.video_br), "100000"));
        videoFramerate = Integer.parseInt(prefs.getString(getString(R.string.video_fr), "15"));
        audioBitrate = Integer.parseInt(prefs.getString(getString(R.string.audio_br), "8000"));
        audioSamplingrate = Integer.parseInt(prefs.getString(getString(R.string.audio_sr), "8000"));
        audioChannels = Integer.parseInt(prefs.getString(getString(R.string.audio_ch), "1"));
        videoWidth = Integer.parseInt(prefs.getString(getString(R.string.video_sz_w), "640"));
        videoHeight = Integer.parseInt(prefs.getString(getString(R.string.video_sz_h), "480"));
        videoMaxDuration = Integer.parseInt(prefs.getString(getString(R.string.max_duration), "1000000"));
        videoMaxFileSize = Integer.parseInt(prefs.getString(getString(R.string.max_filesize), "1000000"));
        
        SERVER_IP = prefs.getString(this.getString(R.string.server_ip), "192.168.0.100");
        FILE_NAME = Utils.getDefaultCacheDir(this).getAbsolutePath() + "/___v_video_encoded.mp4";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, IDM_PREF, Menu.NONE, this.getResources().getString(R.string.settings));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
	        case IDM_PREF: 	Intent intent = new Intent();
	        				intent.setClass(this, Settings.class);
	            			startActivity(intent);
	            			break;
	        default:		return false;
        }
        return true;
    }

    private boolean _previewIsRunning;
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    	if(_previewIsRunning){
    		camera.stopPreview();
    	}

    	try{
    		Camera.Parameters parameters = camera.getParameters();
			//Get the optimal preview size so we don't get an exception when setting the parameters 
    		List<Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
    		Size optimalPreviewSize = getOptimalPreviewSize(supportedPreviewSizes, width, height);
    		parameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
	
    		camera.setParameters(parameters);
			camera.setPreviewDisplay(holder);
		} catch(Exception ex){
			ex.printStackTrace();
			Log.e(this.getClass().getName(), ex.toString());
		}
	
		camera.startPreview();
		_previewIsRunning = true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	openCamera();
    }
    
    
    private void openCamera() {
	   	 try { 	
			 camera = Camera.open();
		     recorder.open();
		 } catch(Exception e) {
			 Log.e("BroadcastView", e.toString());
	     }
    }
    

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	camera.stopPreview();
    	_previewIsRunning = false;
    	camera.release();
    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
       final double ASPECT_TOLERANCE = 0.05;
       double targetRatio = (double) w / h;
       if (sizes == null) return null;

       Size optimalSize = null;
       double minDiff = Double.MAX_VALUE;

       int targetHeight = h;

       // Try to find an size match aspect ratio and size
       for (Size size : sizes) {
           double ratio = (double) size.width / size.height;
           if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
           if (Math.abs(size.height - targetHeight) < minDiff) {
               optimalSize = size;
               minDiff = Math.abs(size.height - targetHeight);
           }
       }

       // Cannot find the one match the aspect ratio, ignore the requirement
       if (optimalSize == null) {
           minDiff = Double.MAX_VALUE;
           for (Size size : sizes) {
               if (Math.abs(size.height - targetHeight) < minDiff) {
                   optimalSize = size;
                   minDiff = Math.abs(size.height - targetHeight);
               }
           }
       }
       return optimalSize;
   }

   @Override
   public void onClick(View v) {
        if(v == btnStart) {
            if(isRecording) {
                recorder.stop();
                try {
                    // deny common access to camera
                    camera.release();
                    // turn on camera preview
                    //camera.startPreview();
                    openCamera();
                } catch (Exception e) {
                	Log.e("BroadcastView", e.getMessage());
                }
                btnStart.setText(this.getResources().getString(R.string.start));
            } else {
            	try {
            		// stop camera preview
            		camera.stopPreview();
            		// allow common access to camera
                	camera.unlock();
	                // recorder uses already created camera
	                recorder.setCamera(camera);
	                // set parameters, preview, file name and turn on record
	                recorder.setRecorderParams(videoBitrate, audioBitrate, audioSamplingrate, audioChannels, videoFramerate, videoWidth, videoHeight, videoMaxDuration, videoMaxFileSize);
	                recorder.setPreview(surfaceHolder.getSurface());
	                
	                recorder.start(FILE_NAME);
	                
	                Thread cThread = new Thread(new UDPClient());
	                cThread.start();
	                btnStart.setText(this.getResources().getString(R.string.stop));
	                
            	} catch (Exception e) {
                    Log.e("BroadcastView", e.toString());
                }
            }
            isRecording = !isRecording;
        }
    }

    /*@Override
    public void onPictureTaken(byte[] paramArrayOfByte, Camera paramCamera) {
        try {
            File saveDir = new File("/sdcard/CameraExample/");
            if(!saveDir.exists()) {
                saveDir.mkdirs();
            }
            FileOutputStream os = new FileOutputStream(String.format(
                    "/sdcard/CameraExample/%d.jpg", System.currentTimeMillis()));
            os.write(paramArrayOfByte);
            os.close();
        } catch(Exception e) {
        }
        paramCamera.startPreview();
    }

    @Override
    public void onAutoFocus(boolean paramBoolean, Camera paramCamera) {
        if(paramBoolean) {
            paramCamera.takePicture(null, null, null, this);
        }
    }*/

    @Override
    public void onPreviewFrame(byte[] paramArrayOfByte, Camera paramCamera) {
        // here we can process the image, displayed in preview
    }
    

    class UDPClient implements Runnable {
        @Override
        public void run() {
            try {
	            //DatagramSocket socket = new DatagramSocket();
	            //byte[] buf = ("CLIENT|test.vms|").getBytes();
	            service.connectMaster("video1.vms");
	            service.connectClient("video1.vms");
            	InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
	            //DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, 8080);
	            //socket.send(packet);
	            
	            Socket socket = new Socket(serverAddr, 80);
            	try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    
                    final File streamFile = File.createTempFile(FILE_NAME, "dat", Utils.getDefaultCacheDir(BroadcastView.this));
        			InputStream stream = new FileInputStream(streamFile);
        			
        			byte buf[] = new byte[COPY_CHUNK_SIZE];
					do {
						int numread = stream.read(buf);
						if (numread <= 0)
							break;
						//buf = Base64.decode(buf, Base64.DEFAULT);
						out.println(buf); //buf.length
					} while (true);
                } catch (Exception e) {
                    Log.e("ClientActivity", "S: Error", e);
                }
	            socket.close();
            } catch (Exception e) {
                Log.e("UDP", "C: Error", e);
            }
        }
    }
    
}
