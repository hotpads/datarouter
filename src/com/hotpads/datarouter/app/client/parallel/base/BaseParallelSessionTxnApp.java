package com.hotpads.datarouter.app.client.parallel.base;

import java.util.Collection;
import java.util.Map;

import javax.persistence.RollbackException;

import com.hotpads.datarouter.app.client.parallel.ParallelSessionApp;
import com.hotpads.datarouter.app.client.parallel.ParallelTxnApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.HibernateClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.exception.DataAccessException;
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
	public T runInEnvironment(){
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
			try{
				rollbackTxns();
				throw new RollbackException(e);
			}catch(Exception rollbackException){
				logger.warn(ExceptionTool.getStackTraceAsString(e));
				throw new DataAccessException("EXCEPTION THROWN DURING TXN ROLL-BACK", e);
			}
		}finally{
			try{
				this.releaseConnections();
			}catch(Exception e){
				//This is an unexpected exception because each individual release is done in a try/catch block
				logger.warn(ExceptionTool.getStackTraceAsString(e));
				throw new DataAccessException("EXCEPTION THROWN DURING RELEASE OF CONNECTIONS:"
						+this.connectionNameByClientName.toString(), e);
			}
		}
		T mergedResult = mergeResults(onceResult, clientResults);
		return mergedResult;
	}
	

	
	/********************* session code **********************************/
	
	@Override
	public void openSessions(){
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof HibernateClient) ){ continue; }
			HibernateClient sessionClient = (HibernateClient)client;
			String connectionName = this.connectionNameByClientName.get(client.getName());
			sessionClient.openSession(connectionName);
		}
	}
	
	@Override
	public void closeSessions(){
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof HibernateClient) ){ continue; }
			HibernateClient sessionClient = (HibernateClient)client;
			String connectionName = this.connectionNameByClientName.get(client.getName());
			sessionClient.closeSession(connectionName);
		}
	}
	
}
