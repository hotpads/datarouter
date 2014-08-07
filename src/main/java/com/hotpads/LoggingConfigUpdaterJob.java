package com.hotpads;


public class LoggingConfigUpdaterJob implements Runnable{

	private String webAppNAme;

	public void setWebAppName(String webAppNAme){
		this.webAppNAme = webAppNAme;
	}

	@Override
	public void run(){
		System.out.println("coucou, I'am " + webAppNAme);
	}

}
