package com.hotpads.datarouter.client.imp.hibernate.op;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.Client;

public interface HibernateOp<T>
//extends DataRouterOp<T>, ClientOp<T>, ConnectionOp<T>, TxnOp<T>, SessionOp<T>
{

	List<String> getClientNames();
	T runOnce();	
	T runOncePerClient(Client client);
	T mergeResults(T fromOnce, Collection<T> fromEachClient);
	
}
