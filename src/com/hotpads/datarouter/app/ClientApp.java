package com.hotpads.datarouter.app;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.Client;

public interface ClientApp<T> 
extends App<T> {

	List<Client> getClients();

	T runOnce() throws Exception;
	T runOncePerClient(Client client) throws Exception;
	
	T mergeResults(T fromOnce, Collection<T> fromEachClient);
	
}
