package com.hotpads.datarouter.app.client.parallel.jdbc.base;

import java.util.List;

import org.hibernate.Session;

import com.hotpads.datarouter.app.SessionOp;
import com.hotpads.datarouter.app.client.parallel.base.BaseParallelSessionOp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.HibernateClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.routing.DataRouterContext;

public abstract class BaseParallelHibernateTxnApp<T>
extends BaseParallelSessionOp<T> 
implements SessionOp<T> {

	public BaseParallelHibernateTxnApp(DataRouterContext drContext, List<String> clientNames) {
		this(drContext, clientNames, Isolation.DEFAULT, false);
	}

	public BaseParallelHibernateTxnApp(DataRouterContext drContext, List<String> clientNames, Isolation isolation,
			boolean autoCommit) {
		super(drContext, clientNames, isolation, autoCommit);
	}

	@Override
	public Session getSession(String clientName){
		Client client = getDataRouterContext().getClientPool().getClient(clientName);
		if(client==null){ return null; }
		if(client instanceof HibernateClient){
			HibernateClient hibernateSessionClient = (HibernateClient)client;
			Session session = hibernateSessionClient.getExistingSession();
			return session;
		}
		return null;
	}

}
