package org.m3.view;

import org.m3.R;
import org.m3.model.Video;
import org.m3.view.tabs.VideoActivity;
import org.m3.view.tabs.CommentsActivity;
import org.m3.view.tabs.ChannelActivity;
import org.m3.xml.DataHandler;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;


public class VideoView extends TabActivity {
	private Video video;

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video);
        
        final ImageView btnBack = (ImageView) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				VideoView.this.finish();
			}
        });
        
        final int categoryId = this.getIntent().getExtras().getInt("categoryId");
        final int videoId = this.getIntent().getExtras().getInt("videoId");
        
        video = DataHandler.getCategories().get(categoryId).getVideos().get(videoId);
        
        TextView name = (TextView) findViewById(R.id.name);
        name.setText(video.getName());

        ImageView rating = (ImageView) findViewById(R.id.rating);
        int resID = this.getResources().getIdentifier("rating"+video.getRating(), "drawable", "org.m3"); //org.anddev.android.
        rating.setImageResource(resID);
        
        ImageView image = (ImageView) findViewById(R.id.image);
        resID = this.getResources().getIdentifier(video.getImage(), "drawable", "org.m3"); //org.anddev.android.
        image.setImageResource(resID);

        TextView description = (TextView) findViewById(R.id.description);
        StringBuilder text = new StringBuilder(16);
        text.append(video.getDescription()).append("\n\n").append(this.getResources().getString(R.string.author))
        		.append(' ').append(video.getAuthor());
        description.setText(text.toString());

        TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);

        TabHost.TabSpec spec;  // Reusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, VideoActivity.class);
        intent.putExtra("categoryId", categoryId);
	    intent.putExtra("videoId", videoId);
	    
	    final TextView dIndicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator, getTabWidget(), false);
	    dIndicator.setText(this.getResources().getString(R.string.video));
        spec = tabHost.newTabSpec("video").setIndicator(dIndicator).setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, CommentsActivity.class);
        intent.putExtra("categoryId", categoryId);
	    intent.putExtra("videoId", videoId);
	    final TextView iIndicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator, getTabWidget(), false);
	    iIndicator.setText(this.getResources().getString(R.string.comments));
        spec = tabHost.newTabSpec("comments").setIndicator(iIndicator).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, ChannelActivity.class);
        intent.putExtra("categoryId", categoryId);
	    intent.putExtra("videoId", videoId);
	    final TextView sIndicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator, getTabWidget(), false);
	    sIndicator.setText(this.getResources().getString(R.string.channel));
        spec = tabHost.newTabSpec("channel").setIndicator(sIndicator).setContent(intent);
        tabHost.addTab(spec);

        tabHost.getTabWidget().getChildAt(0).getLayoutParams().height = 35;
        tabHost.getTabWidget().getChildAt(1).getLayoutParams().height = 35;
        tabHost.getTabWidget().getChildAt(2).getLayoutParams().height = 35;
        
        tabHost.setCurrentTab(0);
        //tabHost.getTabWidget().getChildAt(0).requestFocus();
    }

}
