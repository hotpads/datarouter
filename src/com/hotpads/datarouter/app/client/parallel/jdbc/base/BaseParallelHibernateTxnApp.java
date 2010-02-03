package com.hotpads.datarouter.app.client.parallel.jdbc.base;

import org.hibernate.Session;

import com.hotpads.datarouter.app.HibernateTxnApp;
import com.hotpads.datarouter.app.client.parallel.base.BaseParallelSessionTxnApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.HibernateClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.routing.DataRouter;

public abstract class BaseParallelHibernateTxnApp<T>
extends BaseParallelSessionTxnApp<T> 
implements HibernateTxnApp<T> {

	public BaseParallelHibernateTxnApp(DataRouter router) {
		super(router);
	}
	
	public BaseParallelHibernateTxnApp(DataRouter router, Isolation isolation) {
		super(router, isolation);
	}

	@Override
	public Session getSession(String clientName){
		Client client = this.router.getClient(clientName);
		if(client==null){ return null; }
		if(client instanceof HibernateClient){
			HibernateClient hibernateSessionClient = (HibernateClient)client;
			Session session = hibernateSessionClient.getExistingSession();
			return session;
		}
		return null;
	}

}
