package com.hotpads.datarouter.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.op.aware.DataRouterContextAware;

public interface ClientOp<T>
extends DataRouterContextAware<T>{

	List<String> getClientNames();	
	T runOnce();
	T runOncePerClient(Client client);
	T mergeResults(T fromOnce, Collection<T> fromEachClient);
	
}
