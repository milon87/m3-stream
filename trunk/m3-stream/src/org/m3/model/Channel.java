package org.m3.model;

import java.util.ArrayList;
import java.util.List;


public class Channel {
	private List<Video> videos;

	
	public List<Video> getVideos() {
		if(videos == null) {
			videos = new ArrayList<Video>();
		}
		return videos;
	}
	
}
