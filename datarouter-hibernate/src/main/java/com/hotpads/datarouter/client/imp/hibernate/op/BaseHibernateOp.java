package com.hotpads.datarouter.client.imp.hibernate.op;

import java.util.List;

import org.hibernate.Session;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClient;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.aware.SessionAware;
import com.hotpads.datarouter.routing.DatarouterContext;

public abstract class BaseHibernateOp<T>
extends BaseJdbcOp<T> 
implements SessionAware {
	
	public BaseHibernateOp(DatarouterContext drContext, List<String> clientNames) {
		this(drContext, clientNames, Isolation.DEFAULT, false);
	}

	public BaseHibernateOp(DatarouterContext drContext, List<String> clientNames, Isolation isolation,
			boolean autoCommit) {
		super(drContext, clientNames, isolation, autoCommit);
	}

	@Override
	public Session getSession(String clientName){
		Client client = getDatarouterContext().getClientPool().getClient(clientName);
		if(client==null){
			return null;
		}
		if(client instanceof HibernateClient){
			HibernateClient hibernateSessionClient = (HibernateClient)client;
			Session session = hibernateSessionClient.getExistingSession();
			return session;
		}
		return null;
	}
	
}
