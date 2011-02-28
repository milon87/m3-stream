package org.m3.model;

import java.util.ArrayList;
import java.util.List;


public class Category {
	private final String name;
	private final String description;
	private List<Video> videos;
	
	
	public Category(final String name, final String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		if(description == null) {
			return "";
		} else {
			return description;
		}
	}
	
	public List<Video> getVideos() {
		if(videos == null) {
			videos = new ArrayList<Video>();
		}
		return videos;
	}
	
}
