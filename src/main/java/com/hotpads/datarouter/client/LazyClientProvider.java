package com.hotpads.datarouter.client;

import java.util.concurrent.Callable;

/*
 * call a bunch of these in parallel
 */
public class LazyClientProvider
implements Callable<Client>{
	
	private ClientFactory clientFactory;
	private volatile Client client;

	public LazyClientProvider(ClientFactory clientFactory){
		this.clientFactory = clientFactory;
	}

	//http://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
	@Override
	public Client call(){
		Client result = client;//local variable so we only have to read the volatile once
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
