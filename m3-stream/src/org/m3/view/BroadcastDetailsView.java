package org.m3.view;

import org.m3.R;
import org.m3.Settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class BroadcastDetailsView extends Activity { 
    private static final int IDM_PREF = 101;
    private String category;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.broadcast_details);
        Button btnStream = (Button) findViewById(R.id.btnBroadcast);
        btnStream.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View arg0) {
        		Intent intent = new Intent(BroadcastDetailsView.this, BroadcastView.class);
	        	startActivity(intent);
			}
        });
        
        Spinner categories = (Spinner) findViewById(R.id.spCategories);
        ArrayAdapter<CharSequence> adapterFrom = ArrayAdapter.createFromResource(
                this, R.array.categories, /*R.layout.spinner_layout); //*/android.R.layout.simple_spinner_item);
        adapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categories.setAdapter(adapterFrom);
        categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                category = (String)parent.getItemAtPosition(pos);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        categories.setSelection(0);
        
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

    
}
