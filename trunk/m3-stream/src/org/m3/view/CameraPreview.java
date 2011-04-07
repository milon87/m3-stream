package org.m3.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;

import org.m3.util.Utils;


public class CameraPreview extends Activity {
    private Preview mPreview;
    public static String PATH;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PATH = Utils.getDefaultCacheDir(this).getAbsolutePath();
        Log.i("Temp path:", PATH);
		
        // Create our Preview view and set it as the content of our activity.
        mPreview = new Preview(this);
        setContentView(mPreview);
    }

}

class Preview extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {  
	SurfaceHolder mHolder;  
	Camera mCamera;  
	   
	//This variable is responsible for getting and setting the camera settings  
	private Parameters parameters;  
	//this variable stores the camera preview size  
	private Size previewSize;  
	//this array stores the pixels as hexadecimal pairs  
	private byte[] pixels;  
	 
	Preview(Context context) {  
		super(context);  
	
	    // Install a SurfaceHolder.Callback so we get notified when the  
	    // underlying surface is created and destroyed.  
	    mHolder = getHolder();  
	    mHolder.addCallback(this);  
	    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
	}  
	   
	public void surfaceCreated(SurfaceHolder holder) {  
	    // The Surface has been created, acquire the camera and tell it where  
	    // to draw.  
	    mCamera = Camera.open();  
	    try {  
	    	mCamera.setPreviewDisplay(holder);  
	        //sets the camera callback to be the one defined in this class  
	        mCamera.setPreviewCallback(this);  
	
	        ///initialize the variables  
	        parameters = mCamera.getParameters();  
	        previewSize = parameters.getPreviewSize();  
	        pixels = new byte[previewSize.width * previewSize.height];  
	   
	        socket = new Socket("172.26.24.10", 7778);
	        oos = new ObjectOutputStream(socket.getOutputStream());
    	    //output = new PrintWriter(socket.getOutputStream(), true);
	    } catch (Exception e) {  
	        mCamera.release();  
	        mCamera = null; 
	        Log.e(this.getClass().getName(), e.toString());
	    }  
	}  
	  
	public void surfaceDestroyed(SurfaceHolder holder) {  
	    // Surface will be destroyed when we return, so stop the preview.  
	    // Because the CameraDevice object is not a shared resource, it's very  
	    // important to release it when the activity is paused.  
	    mCamera.stopPreview();  
	    mCamera.release();  
	    mCamera = null;  
	    
	    try {
			socket.close();
		} catch (IOException e) {
			Log.e("BroadcastView", e.toString());
		}
	}  
	
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {  
	    // Now that the size is known, set up the camera parameters and begin  
	    // the preview.  
	    parameters.setPreviewSize(w, h);  
	    //set the camera's settings  
	    mCamera.setParameters(parameters);  
	    mCamera.startPreview();  
	}  
	  
	private Socket socket;
	private ObjectOutputStream oos;
	//private PrintWriter output;
	@Override  
	public void onPreviewFrame(byte[] data, Camera camera) {  
		//transforms NV21 pixel data into RGB pixels  
		try {
			//decodeYUV420SP(pixels, data, previewSize.width,  previewSize.height); 
			SerializedObject so = new SerializedObject();
			so.setArray(data);
        	oos.writeObject(so);
        	oos.flush();
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.toString());
		}
        
        //Log.e("To server", pixels.toString());
	    //output.println(pixels);
	    
	    /*Bitmap bitmap = Bitmap.createBitmap(pixels, previewSize.width, previewSize.height, Config.ARGB_8888);
	    //File sdDir = Environment.getExternalStorageDirectory();
		String fileName = CameraPreview.PATH + "/" + System.currentTimeMillis() + ".jpg";
	    try {
    		FileOutputStream os = new FileOutputStream(fileName);
    		BufferedOutputStream bos = new BufferedOutputStream(os);
    		bitmap.compress(CompressFormat.JPEG, 50, bos);
    		bos.flush();
    		bos.close();
		} catch(Exception e) {
			Log.e(this.getClass().getName(), e.toString());
		}
	    Log.i("Pixels", "File: " + fileName + "; The top right pixel has the following RGB (hexadecimal) values:" + Integer.toHexString(pixels[0]));*/  
	}  
	   
	//Method from Ketai project! Not mine! See below...  
	void decodeYUV420SP(byte[] rgb, byte[] yuv420sp, int width, int height) {  
		final int frameSize = width * height;  
	    for (int j = 0, yp = 0; j < height; j++) {       
	    	int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;  
	    	for (int i = 0; i < width; i++, yp++) {  
	    		int y = (0xff & ((int) yuv420sp[yp])) - 16;  
	            if (y < 0)  
	            	y = 0;  
	            if ((i & 1) == 0) {  
	            	v = (0xff & yuv420sp[uvp++]) - 128;  
	                u = (0xff & yuv420sp[uvp++]) - 128;  
	            }  
	 
	            int y1192 = 1192 * y;  
	            int r = (y1192 + 1634 * v);  
	            int g = (y1192 - 833 * v - 400 * u);  
	            int b = (y1192 + 2066 * u);  
	  
	            if (r < 0)                  r = 0;               else if (r > 262143)  
	            	r = 262143;  
	            if (g < 0)                  g = 0;               else if (g > 262143)  
	            	g = 262143;  
	            if (b < 0)                  b = 0;               else if (b > 262143)  
	                b = 262143;  
	  
	            rgb[yp] = (byte) (0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff));  
	    	}  
	    }  
	}
	
	class SerializedObject implements Serializable {
		private static final long serialVersionUID = -2678155706954136109L;
		private byte[] array = null;
		
		public SerializedObject() { }

		public void setArray(byte array[]) {
			this.array = array;
		}

		public byte[] getArray() {
			return array;
		}
	}

}