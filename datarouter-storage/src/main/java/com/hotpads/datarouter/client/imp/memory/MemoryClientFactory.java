package com.hotpads.datarouter.client.imp.memory;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;


public class MemoryClientFactory implements ClientFactory{

	protected String clientName;
	private final ClientAvailabilitySettings clientAvailabilitySettings;

	public MemoryClientFactory(String clientName, ClientAvailabilitySettings clientAvailabilitySettings){
		this.clientName = clientName;
		this.clientAvailabilitySettings = clientAvailabilitySettings;
	}

	@Override
	public Client call(){
		return new MemoryClient(clientName, clientAvailabilitySettings);
	}

}
