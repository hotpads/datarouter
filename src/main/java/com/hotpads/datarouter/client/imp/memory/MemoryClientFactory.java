package com.hotpads.datarouter.client.imp.memory;

import com.hotpads.datarouter.client.ClientFactory;


public class MemoryClientFactory implements ClientFactory{

	protected String clientName;
	protected boolean initialized = false;
	protected volatile MemoryClient client = null;//volatile for double checked locking
	
	
	public MemoryClientFactory(String clientName){
		this.clientName = clientName;
	}

	
	@Override
	public boolean isInitialized(){
		return initialized;
	}
	
	@Override
	public MemoryClient getClient(){
		if(client!=null){ return client; }//make sure client is volatile
		synchronized(this){
			if(client!=null){ return client; }
			client = new MemoryClient(clientName);
		}
		return client;
	}
	
}
