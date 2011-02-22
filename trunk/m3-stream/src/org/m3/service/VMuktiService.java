package org.m3.service;

import java.io.InputStream;

import org.m3.R;
import org.m3.http.HttpRetriever;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class VMuktiService {
	private final HttpRetriever httpRetriever;
	private final String SERVER_IP;
	
	public VMuktiService(Context context) {
		httpRetriever = new HttpRetriever();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	    SERVER_IP = prefs.getString(context.getString(R.string.server_ip), "http://192.168.0.101");
	    
	}
	
	public void connectClient(String videoName) {
		StringBuilder url = new StringBuilder();
		url.append(SERVER_IP).append("/CLIENT|").append(videoName).append("|");
		
		httpRetriever.retrieve(url.toString());
	}
	
	public String getVideoURI(String videoName) {
		StringBuilder url = new StringBuilder();
		url.append(SERVER_IP).append("/").append(videoName).append("/");
		
		return url.toString();
	}
	
	public InputStream getStream(String videoName) {
		StringBuilder url = new StringBuilder();
		url.append(SERVER_IP).append("/").append(videoName).append("/");
		
		return httpRetriever.retrieveStream(url.toString());
	}
	
}
