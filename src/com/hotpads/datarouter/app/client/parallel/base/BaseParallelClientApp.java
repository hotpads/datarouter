package com.hotpads.datarouter.app.client.parallel.base;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.app.base.BaseApp;
import com.hotpads.datarouter.app.client.parallel.ParallelClientApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.ConnectionClient;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;

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
		List<String> clientNames = getClientNames();
		return router.getClients(clientNames);
	}
	
	@Override
	public abstract T runInEnvironment();

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
			connectionClient.reserveConnection();
//			logger.debug("reserved "+handle);
		}
	}
	
	@Override
	public void releaseConnections(){
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof ConnectionClient) ){ continue; }
			ConnectionClient connectionClient = (ConnectionClient)client;
			try{
				connectionClient.releaseConnection();
//				logger.debug("released "+handle);
			}catch(Exception e){
				logger.warn(ExceptionTool.getStackTraceAsString(e));
				throw new DataAccessException("EXCEPTION THROWN DURING RELEASE OF SINGLE CONNECTION, handle now=:"
						+connectionClient.getExistingHandle(), e);
			}
		}
	}
}
