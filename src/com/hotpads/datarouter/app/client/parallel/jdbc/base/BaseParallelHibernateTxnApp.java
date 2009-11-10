package com.hotpads.datarouter.app.client.parallel.jdbc.base;

import java.sql.SQLException;
import java.util.Map;

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
	
	public BaseParallelHibernateTxnApp(DataRouter router, Map<String,String> existingConnectionNameByClientName) {
		super(router, existingConnectionNameByClientName);
	}
	
	public BaseParallelHibernateTxnApp(DataRouter router, Map<String,String> existingConnectionNameByClientName, Isolation isolation) {
		super(router, existingConnectionNameByClientName, isolation);
	}

	@Override
	public Session getSession(String clientName){
		String connectionName = this.connectionNameByClientName.get(clientName);
		Client client = this.router.getClient(clientName);
		if(client==null){ return null; }
		if(client instanceof HibernateClient){
			HibernateClient hibernateSessionClient = (HibernateClient)client;
			Session session = hibernateSessionClient.getExistingSession(connectionName);
			return session;
		}
		return null;
	}

}
