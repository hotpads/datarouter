package com.hotpads.datarouter.client;


public interface ClientFactory {
	
	boolean isInitialized();
	Client getClient();
	
}
