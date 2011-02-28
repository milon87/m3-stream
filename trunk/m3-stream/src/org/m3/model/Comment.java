package org.m3.model;


public class Comment {
	private String date;
	private String author;
	private String text;
	
	public Comment(String date, String author, String text) {
		this.date = date;
		this.author = author;
		this.text = text;
	}

	public String getDate() {
		if(date == null) date = "";
		return date;
	}

	public String getAuthor() {
		if(author == null) author = "";
		return author;
	}

	public String getText() {
		return text;
	}
	
}
