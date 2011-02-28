package org.m3.view;

import org.m3.R;
import org.m3.view.adapters.VideosAdapter;
import org.m3.xml.DataHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class VideosView extends Activity {
    
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videos);
        
        final int categoryId = this.getIntent().getExtras().getInt("categoryId");
        
        final ImageView btnBack = (ImageView) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				VideosView.this.finish();
			}
        });
        
        TextView title = (TextView) findViewById(R.id.categoryTitle);
        StringBuilder header = new StringBuilder(); 
        header.append(DataHandler.getCategories().get(categoryId).getName());
        String filter = DataHandler.getFilter();
        if(filter!=null && !"".equals(filter)) {
        	header.append(" / ").append(filter);
        }
        title.setText(header.toString());
        
        GridView gridview = (GridView) findViewById(R.id.gridVideos);
        gridview.setAdapter(new VideosAdapter(this, categoryId));

        gridview.setOnItemClickListener(new OnItemClickListener() {
        	@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        		Intent intent = new Intent(VideosView.this, VideoView.class);
        		intent.putExtra("categoryId", categoryId);
      	        intent.putExtra("videoId", position);
      	        startActivityForResult(intent, 0);
            }
        });
    }
}