package org.m3.view;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class CameraPreview1 extends Activity {
    private Preview1 mPreview;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreview = new Preview1(this);
        setContentView(mPreview);
    }
}

class Preview1 extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {  
	SurfaceHolder mHolder;  
	Camera mCamera;  
	private Parameters parameters;  
	private DatagramSocket ds;
	private InetAddress ipAddress;
	private int port;
	
	Preview1(Context context) {  
		super(context);  
	    mHolder = getHolder();  
	    mHolder.addCallback(this);  
	    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
	}  
	   
	public void surfaceCreated(SurfaceHolder holder) {  
	    mCamera = Camera.open();  
	    try {  
	    	mCamera.setPreviewDisplay(holder);  
	        mCamera.setPreviewCallback(this);  
	        parameters = mCamera.getParameters();  
	        ds = new DatagramSocket(7778);
	        ipAddress = InetAddress.getByName("172.26.24.10"); 
	        port = 7778;
	    } catch (Exception e) {  
	        mCamera.release();  
	        mCamera = null; 
	        Log.e(this.getClass().getName(), e.toString());
	    }  
	}  
	  
	public void surfaceDestroyed(SurfaceHolder holder) {  
	    mCamera.stopPreview();  
	    mCamera.release();  
	    mCamera = null;  
	    try {
			ds.close();
		} catch (Exception e) {
			Log.e("BroadcastView", e.toString());
		}
	}  
	
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {  
	    parameters.setPreviewSize(w, h);  
	    mCamera.setParameters(parameters);  
	    mCamera.startPreview();  
	}  
	  
	@Override  
	public void onPreviewFrame(final byte[] data, Camera camera) {  
		try {
			ds.send(new DatagramPacket(data, data.length, ipAddress, port)); 
		} catch (Exception e) {
			Log.e(this.getClass().getName(), e.toString());
		}
		/*new Thread() {
			public void run() {
			
			}
		}.start();*/
	}  

}
