package org.m3.model;

import java.util.ArrayList;
import java.util.List;


public class Video {
	private String image;
	private String name;
	private String author;
	private String description;
	private String url;
	private int rating;
	private List<Comment> comments;
	private Channel channel;
	
	
	public Video(String image, String name, String author, String description, String url, String rating) {
		this.image = image;
		this.name = name;
		this.author = author;
		this.description = description;
		this.url = url;
		try {
			this.rating = Integer.valueOf(rating);
		} catch(Exception e) { // ignore, rating is not defined
		}
	}
	
	public String getImage() {
		return image;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getDescription() {
		if(description == null) {
			return "";
		} else {
			return description;
		}
	}

	public String getUrl() {
		return url;
	}
	
	public int getRating() {
		return rating;
	}
	
	public List<Comment> getComments() {
		if(comments == null) {
			comments = new ArrayList<Comment>();
		}
		return comments;
	}
	
	
	public Channel getChannel() {
		if(channel == null) {
			channel = new Channel();
		}
		return channel;
	}
	
}
