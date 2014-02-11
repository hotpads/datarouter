package com.hotpads.datarouter.client.imp.memory;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;


public class MemoryClientFactory implements ClientFactory{

	protected String clientName;
	
	
	public MemoryClientFactory(String clientName){
		this.clientName = clientName;
	}
	
	@Override
	public Client call(){
		return new MemoryClient(clientName);
	}
	
}
