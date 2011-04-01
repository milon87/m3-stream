package org.m3.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;

import android.view.View;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.hardware.Camera;
import android.hardware.Camera.Size;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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

    
    private Socket socket;
    private PrintWriter output;
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	openCamera();
    	
    	try {
    		socket = new Socket("172.26.24.10", 7777);
    	    output = new PrintWriter(socket.getOutputStream(), true);
    	    //output.println("Hello Android!");
    	    //BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
    	    //read line(s)
    	    //String st = input.readLine();
    	    //Log.i("FROM_SERVER", st);
    	    //Close connection
    	} catch (UnknownHostException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}

    }
    
    
    private void openCamera() {
	   	 try { 	
			 camera = Camera.open();
			 camera.setPreviewCallback(this);
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
    	
	    try {
			socket.close();
		} catch (IOException e) {
			Log.e("BroadcastView", e.toString());
		}
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
    	
    	int width = paramCamera.getParameters().getPictureSize().width;
    	int height = paramCamera.getParameters().getPictureSize().height;
    	output.println(paramArrayOfByte);
    	/*int[] argb8888 =  new int[width*height];
    	decodeYUV(argb8888, paramArrayOfByte, width, height);
    	Bitmap bitmap = Bitmap.createBitmap(argb8888, width, height, Config.ARGB_8888);*/
    }
    
    
	 // decode Y, U, and V values on the YUV 420 buffer described as YCbCr_422_SP by Android 
	 // David Manpearl 081201 
	 public void decodeYUV(int[] out, byte[] fg, int width, int height)
	         throws NullPointerException, IllegalArgumentException {
	     int sz = width * height;
	     if (out == null)
	         throw new NullPointerException("buffer out is null");
	     if (out.length < sz)
	         throw new IllegalArgumentException("buffer out size " + out.length
	                 + " < minimum " + sz);
	     if (fg == null)
	         throw new NullPointerException("buffer 'fg' is null");
	     if (fg.length < sz)
	         throw new IllegalArgumentException("buffer fg size " + fg.length
	                 + " < minimum " + sz * 3 / 2);
	     int i, j;
	     int Y, Cr = 0, Cb = 0;
	     for (j = 0; j < height; j++) {
	         int pixPtr = j * width;
	         final int jDiv2 = j >> 1;
	         for (i = 0; i < width; i++) {
	             Y = fg[pixPtr];
	             if (Y < 0)
	                 Y += 255;
	             if ((i & 0x1) != 1) {
	                 final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
	                 Cb = fg[cOff];
	                 if (Cb < 0)
	                     Cb += 127;
	                 else
	                     Cb -= 128;
	                 Cr = fg[cOff + 1];
	                 if (Cr < 0)
	                     Cr += 127;
	                 else
	                     Cr -= 128;
	             }
	             int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
	             if (R < 0)
	                 R = 0;
	             else if (R > 255)
	                 R = 255;
	             int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1)
	                     + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
	             if (G < 0)
	                 G = 0;
	             else if (G > 255)
	                 G = 255;
	             int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
	             if (B < 0)
	                 B = 0;
	             else if (B > 255)
	                 B = 255;
	             out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
	         }
	     }
	
	 }
	 
	 static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
		    final int frameSize = width * height;

		    for (int j = 0, yp = 0; j < height; j++) {
		        int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
		        for (int i = 0; i < width; i++, yp++) {
		            int y = (0xff & ((int) yuv420sp[yp])) - 16;
		            if (y < 0) y = 0;
		            if ((i & 1) == 0) {
		                v = (0xff & yuv420sp[uvp++]) - 128;
		                u = (0xff & yuv420sp[uvp++]) - 128;
		            }
		            int y1192 = 1192 * y;
		            int r = (y1192 + 1634 * v);
		            int g = (y1192 - 833 * v - 400 * u);
		            int b = (y1192 + 2066 * u);

		            if (r < 0) r = 0; else if (r > 262143) r = 262143;
		            if (g < 0) g = 0; else if (g > 262143) g = 262143;
		            if (b < 0) b = 0; else if (b > 262143) b = 262143;

		            rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
		        }
		    }
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
