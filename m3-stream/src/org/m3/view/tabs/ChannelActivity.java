package org.m3.view.tabs;

import org.m3.R;
import org.m3.model.Video;
import org.m3.model.Channel;
import org.m3.view.EmailView;
import org.m3.xml.DataHandler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;


public class ChannelActivity extends Activity {
    private Video video;    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int categoryId = this.getIntent().getExtras().getInt("categoryId");
        final int videoId = this.getIntent().getExtras().getInt("videoId");
        
        video = DataHandler.getCategories().get(categoryId).getVideos().get(videoId);
        Channel channel = video.getChannel();

        LinearLayout layout = new LinearLayout(this);
    	layout.setOrientation(LinearLayout.VERTICAL);
    	layout.setPadding(0,5,0,20);
    	layout.setBackgroundColor(this.getResources().getColor(R.color.black));
    	layout.getBackground().setDither(true);
    	
		TableLayout table = new TableLayout(this);
		table.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        table.setStretchAllColumns(false);
        
        for(Video i : channel.getVideos()) {
	        TableRow row = new TableRow(this);
	        row.setPadding(0,1,0,0);
        	
	        LinearLayout v = new LinearLayout(this);
	        v.setOrientation(LinearLayout.VERTICAL);
	        
	        LinearLayout h = new LinearLayout(this);
	        h.setOrientation(LinearLayout.HORIZONTAL);
	        
	        ImageView image = new ImageView(this);
            image.setLayoutParams(new GridView.LayoutParams(35, 35));
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setPadding(10, 0, 10, 0);
            int resID = this.getResources().getIdentifier(i.getImage(), "drawable", "org.m3"); //org.anddev.android.
            image.setImageResource(resID);
            h.addView(image);
			
	        TextView unit = new TextView(this);
	        unit.setWidth(70);
	        unit.setPadding(10, 10, 10, 0);
	        unit.setText(i.getName());
	        unit.setTextColor(this.getResources().getColor(R.color.white));
	        unit.setTextSize(12f);
			h.addView(unit);

	        TextView item = new TextView(this);
	        Display display = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
	        int width = display.getWidth(); 
	        item.setWidth(width-70-60);
	        item.setText(i.getDescription());
	        item.setPadding(0, 10, 10, 0);
	        item.setTextColor(this.getResources().getColor(R.color.white));
	        item.setTextSize(12f);
			h.addView(item);

			v.addView(h);
			View view = new View(this);
		    view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 1));
		    view.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.background_grey));
		    v.addView(view);
		     
			row.addView(v);
			table.addView(row);
	    }
        layout.addView(table);
        
        LinearLayout email = new LinearLayout(this);
        email.setPadding(0,20,0,0);
        email.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        email.setGravity(Gravity.RIGHT);
        
        ImageView image = new ImageView(this);
        image.setLayoutParams(new LinearLayout.LayoutParams(40, 40));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setPadding(0, 10, 10, 10);
        image.setImageDrawable(this.getResources().getDrawable(R.drawable.email));
        image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 Intent intent = new Intent(ChannelActivity.this, EmailView.class);
	    	     intent.putExtra("categoryId", categoryId);
	    	     intent.putExtra("videoId", videoId);
	    	     startActivityForResult(intent, 0);
			}
        });
        email.addView(image);
        layout.addView(email);	
        
        setContentView(layout);
    }
    
}