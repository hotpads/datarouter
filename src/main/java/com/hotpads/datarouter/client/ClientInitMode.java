package com.hotpads.datarouter.client;

public enum ClientInitMode {

	lazy,
	eager,
	;
	
	public static ClientInitMode fromString(String in, ClientInitMode defaultMode){
		if(lazy.toString().equals(in)){ return lazy; }
		if(eager.toString().equals(in)){ return eager; }
		return defaultMode; 
	}
	
}
