package com.hotpads.datarouter.app.client.parallel.base;

import java.util.Collection;
import java.util.Map;

import javax.persistence.RollbackException;

import com.hotpads.datarouter.app.client.parallel.ParallelTxnApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.TxnClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public abstract class BaseParallelTxnApp<T>
extends BaseParallelClientApp<T>
implements ParallelTxnApp<T>{

	Isolation isolation = Isolation.repeatableRead;
	
	
	public BaseParallelTxnApp(DataRouter router) {
		super(router);
	}

	public BaseParallelTxnApp(DataRouter router, Map<String,String> existingConnectionNameByClientName) {
		super(router);
		this.connectionNameByClientName.putAll(MapTool.nullSafe(existingConnectionNameByClientName));
	}
	
	public BaseParallelTxnApp(DataRouter router, Map<String,String> existingConnectionNameByClientName, Isolation isolation) {
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
			onceResult = runOnce();
			for(Client client : CollectionTool.nullSafe(clients)){  //TODO threading
				T clientResult = runOncePerClient(client);
				clientResults.add(clientResult);
			}
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
	
	@Override
	public Isolation getIsolation() {
		return Isolation.repeatableRead;
	}
	
	/********************* txn code **********************************/

	@Override
	public void beginTxns() throws Exception{
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof TxnClient) ){ continue; }
			TxnClient txnClient = (TxnClient)client;
			String clientName = client.getName();
			String parentConnectionName = this.parentConnectionNameByClientName.get(client.getName());
			if(parentConnectionName==null){
				parentConnectionName = this.getClass().getSimpleName();
			}
			String connectionName = txnClient.beginTxn(parentConnectionName, this.getIsolation());
			this.connectionNameByClientName.put(clientName, connectionName);
		}
	}
	
	@Override
	public void commitTxns() throws Exception{
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof TxnClient) ){ continue; }
			TxnClient txnClient = (TxnClient)client;
			String clientName = client.getName();
			String connectionName = this.connectionNameByClientName.get(clientName);
			txnClient.commitTxn(connectionName);
		}
	}
	
	@Override
	public void rollbackTxns() throws Exception{
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof TxnClient) ){ continue; }
			TxnClient txnClient = (TxnClient)client;
			String clientName = client.getName();
			String connectionName = this.connectionNameByClientName.get(clientName);
			txnClient.rollbackTxn(connectionName);
		}
	}
	
	
}
