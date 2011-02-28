package org.m3.view.tabs;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.m3.R;
import org.m3.model.Comment;
import org.m3.model.Video;
import org.m3.xml.DataHandler;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;


public class CommentsActivity extends Activity {
    private Video video;   
    private LinearLayout layout;
    private final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int categoryId = this.getIntent().getExtras().getInt("categoryId");
        final int videoId = this.getIntent().getExtras().getInt("videoId");
        
        video = DataHandler.getCategories().get(categoryId).getVideos().get(videoId);
        
    	layout = new LinearLayout(this);
    	layout.setOrientation(LinearLayout.VERTICAL);
    	layout.setPadding(0,5,0,20);
    	layout.setBackgroundColor(this.getResources().getColor(R.color.black));
    	layout.getBackground().setDither(true);
    	
        drawComments();
       
        setContentView(layout);
    }
    
    
    private void drawComments() {
		layout.removeAllViews();
		
		Display display = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
	    int width = display.getWidth(); 
	        
    	TableLayout table = new TableLayout(this);
		table.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        table.setStretchAllColumns(false);
        
    	for(Comment i : video.getComments()) {
	        TableRow row = new TableRow(this);
	        row.setPadding(0,1,0,0);
        	
	        LinearLayout v = new LinearLayout(this);
	        v.setOrientation(LinearLayout.VERTICAL);
	        
	        LinearLayout h = new LinearLayout(this);
	        h.setOrientation(LinearLayout.HORIZONTAL);
	        
	        TextView amount = new TextView(this);
	        amount.setWidth(80);
	        amount.setPadding(10, 0, 10, 0);
	        amount.setText(i.getDate());
	        amount.setTextColor(this.getResources().getColor(R.color.white));
	        amount.setTextSize(7f);
			h.addView(amount);
			
	        TextView unit = new TextView(this);
	        unit.setWidth(50);
	        unit.setPadding(0, 0, 10, 0);
	        unit.setText(i.getAuthor());
	        unit.setTextColor(this.getResources().getColor(R.color.white));
	        unit.setTextSize(12f);
			h.addView(unit);

	        TextView item = new TextView(this);
	        item.setWidth(width-80-50);
	        item.setText(i.getText());
	        item.setPadding(0, 0, 10, 0);
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
        
        // Add comment area
        TableLayout ctable = new TableLayout(this);
        ctable.setPadding(5,30,0,0);
        ctable.setGravity(Gravity.CENTER);
		ctable.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		//ctable.setStretchAllColumns(true);
		TableRow crow = new TableRow(this);
        final EditText edtComment = new EditText(this);
        edtComment.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.button_shape));
        edtComment.setWidth(width-60);
        crow.addView(edtComment);
        Button btnComment = new Button(this);
        btnComment.setTextColor(this.getResources().getColor(R.color.light));
        btnComment.setWidth(50);
        btnComment.setText(this.getResources().getString(R.string.add));
    	btnComment.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.button_shape));
        btnComment.setGravity(Gravity.RIGHT);
        btnComment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				video.getComments().add(new Comment(SDF.format(Calendar.getInstance().getTime()),
						"benny", edtComment.getText().toString()));
				drawComments();
			}
        });
        crow.addView(btnComment);
        ctable.addView(crow);
        layout.addView(ctable);
 
    }
}