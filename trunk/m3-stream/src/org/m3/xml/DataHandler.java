package org.m3.xml;

import java.util.*;

import org.m3.model.Category;
import org.m3.model.Comment;
import org.m3.model.Video;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class DataHandler extends DefaultHandler {
	private final String category = "category";
	private final String description = "description";
	private final String video = "video";
	private final String name = "name";
	private final String image = "image";
	private final String rating = "rating";
	private final String author = "author";
	private final String url = "url";
	private final String date = "date";
	private final String text = "text";
	private final String comment = "comment";
	private final String cvideo = "cvideo";
	
	protected static List<Category> categories;
	private Category category_item;
	private Video video_item;
	
	private Boolean currentElement = false;
	private String currentValue = null;
	
	private static String filter;
	
	
	public DataHandler(String filter) {
		DataHandler.filter = filter;
	}
	
	public static String getFilter() {
		return filter;
	}
	
	public static List<Category> getCategories() {
		if(categories == null) {
			categories = new ArrayList<Category>();
		}
		return categories;
	}
	
    @Override
    public void startElement(String uri, String localName, String qName,
	            Attributes attributes) throws SAXException {
    	currentElement = true;
    	if(localName.equals(category)) {
	    	category_item = new Category(attributes.getValue(name), attributes.getValue(description));
	    } else if(localName.equals(video)) {
	    	video_item = new Video(attributes.getValue(image), attributes.getValue(name), attributes.getValue(author),
	    			attributes.getValue(description), attributes.getValue(url), attributes.getValue(rating));
	    } else if(localName.equals(comment)) {
	    	video_item.getComments().add(new Comment(attributes.getValue(date), 
	    			attributes.getValue(author), attributes.getValue(text)));
	    } else if(localName.equals(cvideo)) {
	    	video_item.getChannel().getVideos().add(new Video(attributes.getValue(image), attributes.getValue(name), attributes.getValue(author),
	    			attributes.getValue(description), attributes.getValue(url), attributes.getValue(rating)));
	    }
	}
    
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		currentElement = false;
	    if(localName.equalsIgnoreCase(video)) {
	    	if(filter != null && !"".equals(filter)) {
	    		if((video_item.getName().toUpperCase().indexOf(filter.toUpperCase()) > 0 
	    				|| video_item.getDescription().toUpperCase().indexOf(filter.toUpperCase()) > 0)) {
	    			category_item.getVideos().add(video_item);
	    		}
	    	} else {
	    		category_item.getVideos().add(video_item);
	    	}
	    } else if(localName.equalsIgnoreCase(category)) {
	    	if(category_item.getVideos().size() > 0) {
	    		getCategories().add(category_item);
	    	}
	    }
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (currentElement) {
			currentValue = new String(ch, start, length);
	        currentElement = false;
	    }
	}

}
