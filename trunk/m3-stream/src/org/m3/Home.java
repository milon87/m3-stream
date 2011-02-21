package org.m3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class Home extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
              
        Button btnVideoRec = (Button) findViewById(R.id.btnVideoRec);
        Button btnVideoView = (Button) findViewById(R.id.btnVideoView);
                
        btnVideoRec.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View arg0) {
	        	Intent intent = new Intent(Home.this, Client.class);
	        	startActivity(intent);
			}
        });
        
        btnVideoView.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View arg0) {
	        	Intent intent = new Intent(Home.this, Viewer.class);
	        	startActivity(intent);
			}
        });
        
           
        
        
        
    }
}