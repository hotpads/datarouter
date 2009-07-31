package com.hotpads.datarouter.app.client.parallel.base;

import java.util.Collection;
import java.util.Map;

import javax.persistence.RollbackException;

import com.hotpads.datarouter.app.client.parallel.ParallelSessionApp;
import com.hotpads.datarouter.app.client.parallel.ParallelTxnApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.HibernateClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public abstract class BaseParallelSessionTxnApp<T>
extends BaseParallelTxnApp<T>
implements ParallelTxnApp<T>, ParallelSessionApp<T>{
	
	
	public BaseParallelSessionTxnApp(DataRouter router) {
		super(router);
	}

	public BaseParallelSessionTxnApp(DataRouter router, Map<String,String> existingConnectionNameByClientName) {
		super(router);
		this.connectionNameByClientName.putAll(MapTool.nullSafe(existingConnectionNameByClientName));
	}
	
	public BaseParallelSessionTxnApp(DataRouter router, Map<String,String> existingConnectionNameByClientName, Isolation isolation) {
		super(router);
		this.connectionNameByClientName.putAll(MapTool.nullSafe(existingConnectionNameByClientName));
		this.isolation = isolation;
	}


	/*******************************************************************/

	
	@Override
	public T runInEnvironment() throws Exception{
		T onceResult = null;
		Collection<T> clientResults = ListTool.createLinkedList();
		Collection<Client> clients = this.getClients();
		try{
			beginTxns();
			openSessions();
			
			//begin user code
			onceResult = runOnce();
			for(Client client : CollectionTool.nullSafe(clients)){  //TODO threading
				T clientResult = runOncePerClient(client);
				clientResults.add(clientResult);
			}
			//end user code
			
			closeSessions();
			commitTxns();
		}catch(Exception e){
			logger.warn(ExceptionTool.getStackTraceAsString(e));
			rollbackTxns();
			throw new RollbackException(e);
		}finally{
			this.releaseConnections();
		}
		T mergedResult = mergeResults(onceResult, clientResults);
		return mergedResult;
	}
	

	
	/********************* session code **********************************/
	
	@Override
	public void openSessions() throws Exception{
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof HibernateClient) ){ continue; }
			HibernateClient sessionClient = (HibernateClient)client;
			String connectionName = this.connectionNameByClientName.get(client.getName());
			sessionClient.openSession(connectionName);
		}
	}
	
	@Override
	public void closeSessions() throws Exception{
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof HibernateClient) ){ continue; }
			HibernateClient sessionClient = (HibernateClient)client;
			String connectionName = this.connectionNameByClientName.get(client.getName());
			sessionClient.closeSession(connectionName);
		}
	}
	
}
