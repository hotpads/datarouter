package com.hotpads.datarouter.client;

import java.util.concurrent.Callable;

import com.hotpads.datarouter.node.DatarouterNodes;
import com.hotpads.util.core.concurrent.Lazy;

/*
 * call a bunch of these in parallel
 */
public class LazyClientProvider implements Callable<Client>{

	private final Lazy<Client> client;

	public LazyClientProvider(ClientFactory clientFactory, DatarouterNodes datarouterNodes){
		this.client = Lazy.of(() -> {
			try{
				Client client = clientFactory.call();
				datarouterNodes.getPhysicalNodesForClient(client.getName()).forEach(client::notifyNodeRegistration);
				return client;
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public Client call(){
		return client.get();
	}

	//used by datarouterMenu.jsp
	public boolean isInitialized(){
		return client.isInitialized();
	}

	//bean accessor used by datarouterMenu.jsp
	public Client getClient(){
		return call();
	}

}
