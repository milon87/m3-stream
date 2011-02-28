package org.m3.view.adapters;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import org.m3.R;
import org.m3.model.Category;
import org.m3.xml.DataHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


public class CategoriesAdapter extends BaseAdapter {
    private Context mContext;
    private List<Category> categories;
    private String filter;
    
    
    public CategoriesAdapter(Context c, String filter) {
        this.mContext = c;
        this.filter = filter;
    }

    public int getCount() {
        return getCategories().size();
    }

    public Object getItem(int position) {
        return getCategories().get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
    	LinearLayout layout;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
        	layout = new LinearLayout(mContext);
        	layout.setOrientation(LinearLayout.VERTICAL);
        	layout.setPadding(0,5,0,5);
        	layout.setBackgroundColor(mContext.getResources().getColor(R.color.black));
        	
        	TextView name = new TextView(mContext);
            name.setText(getCategories().get(position).getName());
            name.setPadding(10, 2, 0, 0);
            name.setTextColor(mContext.getResources().getColor(R.color.white));
        	layout.addView(name);
            
            TableLayout inner = new TableLayout(mContext);
            inner.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        	inner.setStretchAllColumns(true);
        	TableRow row = new TableRow(mContext);
        	
            	TextView description = new TextView(mContext);
            	String d = getCategories().get(position).getDescription();
            	d = d.length() > 30 ? d.substring(0, 30) + "..." : d;
            	description.setText(d);
 	            description.setPadding(8, 2, 0, 0);
		        description.setTextSize(10f);
		        description.setTextColor(mContext.getResources().getColor(R.color.light));
		        row.addView(description);
		        
		        TextView recipes = new TextView(mContext);
		        recipes.setGravity(Gravity.RIGHT);
		        StringBuilder amount = new StringBuilder(8);
		        amount.append(getCategories().get(position).getVideos().size()).append(' ')
		        	.append(mContext.getResources().getString(R.string.videos));
		        recipes.setText(amount.toString());
		        recipes.setPadding(0, 2, 8, 0);
		        recipes.setTextSize(10f);
		        recipes.setTextColor(mContext.getResources().getColor(R.color.light));
		        row.addView(recipes);
		    inner.addView(row);
		    layout.addView(inner);
        } else {
            layout = (LinearLayout) convertView;
        }
        
        return layout;
    }

    
    private List<Category> getCategories() {
    	if(categories == null) {
    		DataHandler.getCategories().clear();
    		categories = parseCategoriesFromXML();
    	} 
    	return categories;
    }

    private List<Category> parseCategoriesFromXML() {
    	List<Category> ret;
    	try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            //URL sourceUrl = new URL("http://www.xxx.com/categories.xml");
            DataHandler categoriesHandler = new DataHandler(filter);
            xr.setContentHandler(categoriesHandler);
            xr.parse(new InputSource(mContext.getResources().openRawResource(R.raw.videos)));
            //xr.parse(new InputSource(sourceUrl.openStream()));

            ret = DataHandler.getCategories();
        } catch (Exception e) {
            Log.e("XML Pasing Exception", e.getMessage());
            ret = new ArrayList<Category>();
        }
        return ret;
    }
    
}
