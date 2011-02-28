package org.m3.view.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import org.m3.R;
import org.m3.model.Video;
import org.m3.xml.DataHandler;


public class VideosAdapter extends BaseAdapter {
    private Context mContext;
    private int categoryId;
    
    
    public VideosAdapter(Context c, int categoryId) {
        this.mContext = c;
        this.categoryId = categoryId;
    }

    public int getCount() {
        return DataHandler.getCategories().get(categoryId).getVideos().size();
    }

    public Object getItem(int position) {
        return DataHandler.getCategories().get(categoryId).getVideos().get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
    	LinearLayout layout;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
        	layout = new LinearLayout(mContext);
        	layout.setOrientation(LinearLayout.HORIZONTAL);
        	layout.setBackgroundColor(mContext.getResources().getColor(R.color.black));
        	
        	if(DataHandler.getCategories().get(categoryId).getVideos().size() > position) {
	        	Video r = DataHandler.getCategories().get(categoryId).getVideos().get(position);
	        	
	        	ImageView image = new ImageView(mContext);
	            image.setLayoutParams(new GridView.LayoutParams(55, 55));
	            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
	            image.setPadding(8, 8, 8, 8);
	            int resID = mContext.getResources().getIdentifier(r.getImage(), "drawable", "org.m3"); //org.anddev.android.
	            image.setImageResource(resID);
	            layout.addView(image);
	        
	        	LinearLayout innerV = new LinearLayout(mContext);
	        	innerV.setOrientation(LinearLayout.VERTICAL);
	            innerV.setPadding(0, 8, 0, 8);
	        		TableLayout table = new TableLayout(mContext);
		        	table.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		            table.setStretchAllColumns(true);
		            TableRow row1 = new TableRow(mContext);
	        		
		            TextView name = new TextView(mContext);
		            name.setText(r.getName());
		            name.setTextColor(mContext.getResources().getColor(R.color.white));
		            name.setTextSize(12f);
					row1.addView(name);
					
				    LinearLayout l = new LinearLayout(mContext);
				    l.setGravity(Gravity.RIGHT);
				    l.setPadding(0,3,8,0);
					ImageView rating = new ImageView(mContext);
				    resID = mContext.getResources().getIdentifier("rating"+r.getRating(), "drawable", "org.m3"); //org.anddev.android.
	            	rating.setImageResource(resID);
	            	l.addView(rating);
	            	row1.addView(l);
		            table.addView(row1);
		            
		            TableRow row2 = new TableRow(mContext);
		            TextView description = new TextView(mContext);
			        description.setPadding(0, 10, 0, 0);
			        description.setText(r.getDescription().length() > 30 ? r.getDescription().substring(0, 30) + "..." : r.getDescription());
		            description.setTextColor(mContext.getResources().getColor(R.color.light));
		            description.setTextSize(12f);
			        row2.addView(description);
			        
			        TextView author = new TextView(mContext);
			        author.setPadding(0, 0, 8, 0);
			        author.setGravity(Gravity.RIGHT);
			        author.setText(r.getAuthor());
			        author.setTextColor(mContext.getResources().getColor(R.color.light));
			        author.setTextSize(12f);
			        row2.addView(author);
			        
			        table.addView(row2);
		            
		        innerV.addView(table);
		        layout.addView(innerV);
		    }
        } else {
            layout = (LinearLayout) convertView;
        }
        
        return layout;
    }

}