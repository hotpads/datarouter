package com.hotpads.handler.https;

public enum UrlScheme{
	HTTP("http"), HTTPS("https"), ANY("any");

	protected final String stringRepresentation;

	private UrlScheme(String stringRepresentation){
		this.stringRepresentation = stringRepresentation;
	}

	public String getStringRepresentation(){
		return stringRepresentation;
	}
}