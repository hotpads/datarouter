package com.hotpads.datarouter.op.executor;

import java.util.List;

import com.hotpads.datarouter.client.Client;


public interface ClientExecutor{

	List<Client> getClients();
	
	void reserveConnections();
	void releaseConnections();
	
}
