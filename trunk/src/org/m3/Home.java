package org.m3;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View;

import android.hardware.Camera;
import android.hardware.Camera.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class Home extends Activity implements SurfaceHolder.Callback,
        View.OnClickListener, Camera.PictureCallback, Camera.PreviewCallback,
        Camera.AutoFocusCallback {

    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private SurfaceView preview;
    private VideoRecorder recorder;
    
    private Button shotBtn;
    private Button recordBtn;
    private Boolean isRecording = false;

    private static final int IDM_PREF = 101;
    private static final int IDM_EXIT = 102;
    
    private int videoBitrate;
    private int videoFramerate;
    private int audioBitrate;
    private int audioSamplingrate;
    private int audioChannels;
    private int videoWidth;
    private int videoHeight;
    private int videoMaxDuration;
    private int videoMaxFileSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set explicitly the orientation type
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // make the application fullscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // without title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

        // our SurfaceView has name SurfaceView01
        preview = (SurfaceView) findViewById(R.id.SurfaceView01);

        surfaceHolder = preview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // button named Button01
        shotBtn = (Button) findViewById(R.id.Button01);
        shotBtn.setText("Shot");
        shotBtn.setOnClickListener(this);

        recordBtn = (Button) findViewById(R.id.Button02);
        recordBtn.setText("Start");
        recordBtn.setOnClickListener(this);

        recorder = new VideoRecorder();
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open();
        recorder.open();
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        videoBitrate = Integer.parseInt(prefs.getString(getString(R.string.video_br), "100000"));
        videoFramerate = Integer.parseInt(prefs.getString(getString(R.string.video_fr), "15"));
        audioBitrate = Integer.parseInt(prefs.getString(getString(R.string.audio_br), "8000"));
        audioSamplingrate = Integer.parseInt(prefs.getString(getString(R.string.audio_sr), "8000"));
        audioChannels = Integer.parseInt(prefs.getString(getString(R.string.audio_ch), "1"));
        videoWidth = Integer.parseInt(prefs.getString(getString(R.string.video_sz_w), "640"));
        videoHeight = Integer.parseInt(prefs.getString(getString(R.string.video_sz_h), "480"));
        videoMaxDuration = Integer.parseInt(prefs.getString(getString(R.string.max_duration), "0"));
        videoMaxFileSize = Integer.parseInt(prefs.getString(getString(R.string.max_filesize), "0"));
    }

    @Override
    protected void onPause() {
        super.onPause();

        recorder.close();

        if(camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, IDM_PREF, Menu.NONE, "Settings");
        menu.add(Menu.NONE, IDM_EXIT, Menu.NONE, "Exit");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
	        case IDM_PREF:
	        	Intent intent = new Intent();
	            intent.setClass(this, SettingsScreen.class);
	            startActivity(intent);
	            break;
	        case IDM_EXIT:
	            finish();
	            break;
	        default:
	        	return false;
        }

        return true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(this);
        } catch(IOException e) {
            e.printStackTrace();
        }

        Size previewSize = camera.getParameters().getPreviewSize();
        float aspect = (float) previewSize.width / previewSize.height;

        int previewSurfaceWidth = preview.getWidth();
        int previewSurfaceHeight = preview.getHeight();

        LayoutParams lp = preview.getLayoutParams();

        // there we make correction of displayed preview size to prevent violations
        if(this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            // portrait view
            camera.setDisplayOrientation(90);
            lp.height = previewSurfaceHeight;
            lp.width = (int) (previewSurfaceHeight / aspect);
        } else {
            // landscape view
            camera.setDisplayOrientation(0);
            lp.width = previewSurfaceWidth;
            lp.height = (int) (previewSurfaceWidth / aspect);
        }

        preview.setLayoutParams(lp);
        camera.startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onClick(View v) {
        if(v == shotBtn) {
            // either we are doing a snapshot there
            // or turning on authophocus processing

            // camera.takePicture(null, null, null, this);
            camera.autoFocus(this);
        } else if(v == recordBtn) {
            if(isRecording) {
                recorder.stop();

                try {
                    // reject public access to camera
                    camera.reconnect();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // turn on again camera preview
                camera.startPreview();

                recordBtn.setText("Record");
                
                // turn on autosnapshot button
                shotBtn.setEnabled(true);
            } else {
                // turn off autosnapshot button
                shotBtn.setEnabled(false);
                
                // stop camera preview (to prevent an error)
                camera.stopPreview();
                
                // allow public access to camera
                camera.unlock();

                // recorder uses already created camera
                recorder.setCamera(camera);
                
                // set parameters, preview, file name and turn on the record
                recorder.setRecorderParams(videoBitrate, audioBitrate, audioSamplingrate, audioChannels, videoFramerate, videoWidth, videoHeight, videoMaxDuration, videoMaxFileSize);
                recorder.setPreview(surfaceHolder.getSurface());
                recorder.start(String.format("/sdcard/CameraExample/%d.mp4", System.currentTimeMillis()));
                
                recordBtn.setText("Stop");
            }

            isRecording = !isRecording;
        }
    }

    @Override
    public void onPictureTaken(byte[] paramArrayOfByte, Camera paramCamera) {
        // save obtained jpg's in folder /sdcard/CameraExample/
        // file name - System.currentTimeMillis()

        try {
            File saveDir = new File("/sdcard/CameraExample/");

            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }

            FileOutputStream os = new FileOutputStream(String.format(
                    "/sdcard/CameraExample/%d.jpg", System.currentTimeMillis()));
            os.write(paramArrayOfByte);
            os.close();
        } catch(Exception e) {
        }

        // after snapshot is ready, the preview displaying is turned off
        // we need to turn it on
        paramCamera.startPreview();
    }

    @Override
    public void onAutoFocus(boolean paramBoolean, Camera paramCamera) {
        if(paramBoolean) {
            // if we focused successfully, make the snapshot
            paramCamera.takePicture(null, null, null, this);
        }
    }

    @Override
    public void onPreviewFrame(byte[] paramArrayOfByte, Camera paramCamera) {
        // here we can process the image that will be displayed in preview
    }
}
