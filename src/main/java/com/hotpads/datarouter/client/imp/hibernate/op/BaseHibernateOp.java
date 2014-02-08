package com.hotpads.datarouter.client.imp.hibernate.op;

import java.util.List;

import org.hibernate.Session;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.type.JdbcClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.aware.SessionAware;
import com.hotpads.datarouter.routing.DataRouterContext;

public abstract class BaseHibernateOp<T>
extends BaseJdbcOp<T> 
implements SessionAware<T> {
	
	public BaseHibernateOp(DataRouterContext drContext, List<String> clientNames) {
		this(drContext, clientNames, Isolation.DEFAULT, false);
	}

	public BaseHibernateOp(DataRouterContext drContext, List<String> clientNames, Isolation isolation,
			boolean autoCommit) {
		super(drContext, clientNames, isolation, autoCommit);
	}

	@Override
	public Session getSession(String clientName){
		Client client = getDataRouterContext().getClientPool().getClient(clientName);
		if(client==null){ return null; }
		if(client instanceof JdbcClient){
			JdbcClient hibernateSessionClient = (JdbcClient)client;
			Session session = hibernateSessionClient.getExistingSession();
			return session;
		}
		return null;
	}
	
}
