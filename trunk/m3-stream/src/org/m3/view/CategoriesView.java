package org.m3.view;

import org.m3.R;
import org.m3.view.adapters.CategoriesAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;


public class CategoriesView extends Activity {
    
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories);
        
        final Button btnBroadcast = (Button) findViewById(R.id.btnBroadcast);
        btnBroadcast.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
    	        Intent intent = new Intent(CategoriesView.this, BroadcastDetailsView.class);
    	        startActivityForResult(intent, 0);
			}
        });
        
        final GridView gridview = (GridView) findViewById(R.id.gridCategories);
        gridview.setAdapter(new CategoriesAdapter(this, ""));
        gridview.getBackground().setDither(true);

        gridview.setOnItemClickListener(new OnItemClickListener() {
        	@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    	        Intent intent = new Intent(CategoriesView.this, VideosView.class);
    	        intent.putExtra("categoryId", position);
    	        startActivityForResult(intent, 0);
            }
        });

    
        final EditText edtSearch = (EditText) findViewById(R.id.edtSearch);
        
        final ImageView btnSearch = (ImageView) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				gridview.setAdapter(new CategoriesAdapter(CategoriesView.this, edtSearch.getText().toString()));
			}
        });
    }

}
