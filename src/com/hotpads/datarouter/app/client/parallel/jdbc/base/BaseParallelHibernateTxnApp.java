package com.hotpads.datarouter.app.client.parallel.jdbc.base;

import java.util.List;

import org.hibernate.Session;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.jdbc.ddl.op.BaseJdbcTxnOp;
import com.hotpads.datarouter.client.type.HibernateClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.aware.SessionAware;
import com.hotpads.datarouter.routing.DataRouterContext;

//TODO rename BaseHibernateTxnOp and move to hibernate module
public abstract class BaseParallelHibernateTxnApp<T>
extends BaseJdbcTxnOp<T> 
implements SessionAware<T> {
	
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
