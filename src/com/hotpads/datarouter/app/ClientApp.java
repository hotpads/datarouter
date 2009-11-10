package com.hotpads.datarouter.app;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.Client;

public interface ClientApp<T> 
extends App<T> {

	List<Client> getClients();

	T runOnce();
	T runOncePerClient(Client client);
	
	T mergeResults(T fromOnce, Collection<T> fromEachClient);
	
}
