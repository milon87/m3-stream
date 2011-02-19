package org.m3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Home extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
              
        TextView MainHdr = (TextView) findViewById(R.id.MainHdr);
        MainHdr.setPadding(0,20,0,0);
        
        Button btnVideoRec = (Button) findViewById(R.id.btnVideoRec);
        Button btnVideoView = (Button) findViewById(R.id.btnVideoView);
                
        btnVideoRec.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View arg0) {
	        	Intent intent = new Intent(Home.this, MainScreen.class);
	        	startActivity(intent);
			}
        });
        
        btnVideoView.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View arg0) {
	        	Intent intent = new Intent(Home.this, VideoViewer.class);
	        	startActivity(intent);
			}
        });
        
           
        
        
        
    }
}