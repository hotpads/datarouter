package com.hotpads.datarouter.client;

import java.util.concurrent.Callable;

/*
 * call a bunch of these in parallel
 */
public class LazyClientProvider
implements Callable<Client>{
	
	private ClientFactory clientFactory;
	
	//volatile to ensure atomic construction: http://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
	private volatile Client client;

	public LazyClientProvider(ClientFactory clientFactory){
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
	
	public boolean isInitialized(){
		return client != null;
	}
}
