package org.m3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class Client extends Activity { 
    private static final int IDM_PREF = 101;
    private static final int IDM_EXIT = 102;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.client);
        Button btnStream = (Button) findViewById(R.id.btnStream);
        btnStream.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View arg0) {
        		Intent intent = new Intent(Client.this, Encoder.class);
	        	startActivity(intent);
			}
        });
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
	        case IDM_PREF: 	Intent intent = new Intent();
	        				intent.setClass(this, Settings.class);
	            			startActivity(intent);
	            			break;
	        case IDM_EXIT:  finish();
	            			break;
	        default:		return false;
        }
        return true;
    }

    
}
