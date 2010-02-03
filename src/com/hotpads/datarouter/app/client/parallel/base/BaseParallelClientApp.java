package com.hotpads.datarouter.app.client.parallel.base;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.app.base.BaseApp;
import com.hotpads.datarouter.app.client.parallel.ParallelClientApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.ConnectionClient;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;

public abstract class BaseParallelClientApp<T>
extends BaseApp<T>
implements ParallelClientApp<T>{
	
	
	public BaseParallelClientApp(DataRouter router) {
		super(router);
	}
	
	/************* app ******************************************************/

	public abstract List<String> getClientNames();
	
	@Override
	public List<Client> getClients() {
		return this.router.getClients(this.getClientNames());
	}
	
	@Override
	public abstract T runInEnvironment();
//	{
//		T onceResult = null;
//		Collection<T> clientResults = ListTool.createLinkedList();
//		Collection<Client> clients = this.getClients();
//		try{
//			reserveConections();
//			
//			//begin abstract user methods
//			onceResult = runOnce();
//			for(Client client : CollectionTool.nullSafe(clients)){
//				T clientResult = runOncePerClient(client);
//				clientResults.add(clientResult);
//			}
//			//end abstract user methods 
//			
//		}finally{
//			try{
//				releaseConnections();
//			}catch(Exception e){
//				//This is an unexpected exception because each individual release is done in a try/catch block
//				logger.warn(ExceptionTool.getStackTraceAsString(e));
//				throw new DataAccessException("EXCEPTION THROWN DURING RELEASE OF CONNECTIONS", e);
//			}
//		}
//		T mergedResult = mergeResults(onceResult, clientResults);
//		return mergedResult;
//	}

	@Override
	public T runOnce(){  //probably used sometimes
		return null;
	}
	
	@Override
	public T runOncePerClient(Client client){  //probably used always
		return null;
	}
	
	@Override  //users probably need to override if they care about the return value
	public T mergeResults(T fromOnce, Collection<T> fromEachClient) {
		return fromOnce;  //hard to do much else without knowing anything about T
		
		//TODO create a Mergeable base-class for merging results from partitions, or always return a Collection<T>
	}
	

	/********************* txn code **********************************/

	@Override
	public void reserveConections(){
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof ConnectionClient) ){ continue; }
			ConnectionClient connectionClient = (ConnectionClient)client;
			ConnectionHandle handle = connectionClient.reserveConnection();
//			logger.debug("reserved "+handle);
		}
	}
	
	@Override
	public void releaseConnections(){
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof ConnectionClient) ){ continue; }
			ConnectionClient connectionClient = (ConnectionClient)client;
			try{
				ConnectionHandle handle = connectionClient.releaseConnection();
//				logger.debug("released "+handle);
			}catch(Exception e){
				logger.warn(ExceptionTool.getStackTraceAsString(e));
				throw new DataAccessException("EXCEPTION THROWN DURING RELEASE OF SINGLE CONNECTION, handle now=:"
						+connectionClient.getExistingHandle(), e);
			}
		}
	}
}
