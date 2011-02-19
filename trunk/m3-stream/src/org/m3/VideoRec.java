package org.m3;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.view.View;

import android.hardware.Camera;
import android.hardware.Camera.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class VideoRec extends Activity implements SurfaceHolder.Callback,
        View.OnClickListener, Camera.PictureCallback, Camera.PreviewCallback,
        Camera.AutoFocusCallback
{

    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private SurfaceView preview;
    private VRecorder recorder;
    
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
    private MediaPlayer mp;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.video_rec);
        
        Button btnHome = (Button) findViewById(R.id.btnHome);
                
        btnHome.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View arg0) {
        		if(mp!=null) mp.stop();
            	finish();
			}
        });
        
        // если хотим, чтобы приложение постоянно имело портретную ориентацию
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // если хотим, чтобы приложение было полноэкранным
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // и без заголовка
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // наше SurfaceView имеет имя SurfaceView01
        preview = (SurfaceView) findViewById(R.id.SurfaceView01);

        surfaceHolder = preview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // кнопка имеет имя Button01
        shotBtn = (Button) findViewById(R.id.Button01);
        shotBtn.setText("Shot");
        shotBtn.setOnClickListener(this);

        recordBtn = (Button) findViewById(R.id.Button02);
        recordBtn.setText("Start");
        recordBtn.setOnClickListener(this);

        recorder = new VRecorder();
    }

    @Override
    protected void onResume()
    {
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
    protected void onPause()
    {
        super.onPause();

        recorder.close();

        if (camera != null)
        {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(Menu.NONE, IDM_PREF, Menu.NONE, "Settings");
        menu.add(Menu.NONE, IDM_EXIT, Menu.NONE, "Exit");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
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
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        try
        {
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(this);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Size previewSize = camera.getParameters().getPreviewSize();
        float aspect = (float) previewSize.width / previewSize.height;

        int previewSurfaceWidth = preview.getWidth();
        int previewSurfaceHeight = preview.getHeight();

        LayoutParams lp = preview.getLayoutParams();

        // здесь корректируем размер отображаемого preview, чтобы не было
        // искажений

        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
        {
            // портретный вид
            camera.setDisplayOrientation(90);
            lp.height = previewSurfaceHeight;
            lp.width = (int) (previewSurfaceHeight / aspect);
        }
        else
        {
            // ландшафтный
            camera.setDisplayOrientation(0);
            lp.width = previewSurfaceWidth;
            lp.height = (int) (previewSurfaceWidth / aspect);
        }

        preview.setLayoutParams(lp);
        camera.startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
    }

    @Override
    public void onClick(View v)
    {
        if (v == shotBtn)
        {
            // либо делаем снимок непосредственно здесь
            // либо включаем обработчик автофокуса

            // camera.takePicture(null, null, null, this);
            camera.autoFocus(this);
        }
        else if (v == recordBtn)
        {
            if (isRecording)
            {
                recorder.stop();

                try
                {
                    // запрещаем общий доступ к камере
                    camera.reconnect();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // снова включаем preview камеры
                camera.startPreview();

                recordBtn.setText("Record");
                
                // включаем кнопку фотосъемки
                shotBtn.setEnabled(true);
            }
            else
            {
                // выключаем кнопку фотосъемки
                shotBtn.setEnabled(false);
                
                // останавливаем preview камеры (иначе будет ошибка)
                camera.stopPreview();
                
                // разрешаем общий доступ к камере
                camera.unlock();

                // рекордер использует уже созданную камеру
                recorder.setCamera(camera);
                
                // задаем параметры, preview, имя файла и включаем запись
                recorder.setRecorderParams(videoBitrate, audioBitrate, audioSamplingrate, audioChannels, videoFramerate, videoWidth, videoHeight, videoMaxDuration, videoMaxFileSize);
                recorder.setPreview(surfaceHolder.getSurface());
                recorder.start(String.format("/sdcard/CameraExample/%d.mp4", System.currentTimeMillis()));
                
                recordBtn.setText("Stop");
            }

            isRecording = !isRecording;
        }
    }

    @Override
    public void onPictureTaken(byte[] paramArrayOfByte, Camera paramCamera)
    {
        // сохраняем полученные jpg в папке /sdcard/CameraExample/
        // имя файла - System.currentTimeMillis()

        try
        {
            File saveDir = new File("/sdcard/CameraExample/");

            if (!saveDir.exists())
            {
                saveDir.mkdirs();
            }

            FileOutputStream os = new FileOutputStream(String.format(
                    "/sdcard/CameraExample/%d.jpg", System.currentTimeMillis()));
            os.write(paramArrayOfByte);
            os.close();
        }
        catch (Exception e)
        {
        }

        // после того, как снимок сделан, показ превью отключается. необходимо
        // включить его
        paramCamera.startPreview();
    }

    @Override
    public void onAutoFocus(boolean paramBoolean, Camera paramCamera)
    {
        if (paramBoolean)
        {
            // если удалось сфокусироваться, делаем снимок
            paramCamera.takePicture(null, null, null, this);
        }
    }

    @Override
    public void onPreviewFrame(byte[] paramArrayOfByte, Camera paramCamera)
    {
        // здесь можно обрабатывать изображение, показываемое в preview
    }
}
