package com.hotpads.datarouter.app.parallel;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.Client;

public interface ParallelClientOp<T>{

	List<Client> getClients();
	
	void reserveConnections();
	void releaseConnections();
	

	T runOnce();
	T runOncePerClient(Client client);
	
	T mergeResults(T fromOnce, Collection<T> fromEachClient);
	
}
