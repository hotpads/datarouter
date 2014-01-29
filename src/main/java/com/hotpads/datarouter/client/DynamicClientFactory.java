package com.hotpads.datarouter.client;


public interface DynamicClientFactory 
extends ClientFactory{
	
	boolean shouldReconnect();
	
}
