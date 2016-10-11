package com.hotpads.job.web;

public class BasicJobCategory implements JobCategory{

	private final String display;
	private final String persistentString;

	public BasicJobCategory(String display, String persistentString){
		this.display = display;
		this.persistentString = persistentString;
	}

	@Override
	public String getDisplay(){
		return display;
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

}