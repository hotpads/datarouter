package com.hotpads.datarouter.client;

import java.util.concurrent.Callable;

public class LazyClientInitializer
implements Callable<Client>{
	
	private ClientFactory clientFactory;
	private volatile Client client;

	public LazyClientInitializer(ClientFactory clientFactory){
		this.clientFactory = clientFactory;
	}

	
	@Override
	public Client call(){
		if(client != null){ return client; }//lightweight volatile check
		synchronized(this){
			if(client != null){ return client; }
			// logger.warn("activating Jdbc client "+clientName);
			try{
				client = clientFactory.call();
			}catch(Exception e){
				throw new RuntimeException(e);
			}
			return client;
		}
	}
}
