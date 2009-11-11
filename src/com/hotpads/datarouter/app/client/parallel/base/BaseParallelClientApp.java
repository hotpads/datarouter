package com.hotpads.datarouter.app.client.parallel.base;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.app.base.BaseApp;
import com.hotpads.datarouter.app.client.parallel.ParallelClientApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.ConnectionClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public abstract class BaseParallelClientApp<T>
extends BaseApp<T>
implements ParallelClientApp<T>{

	protected Map<String,String> parentConnectionNameByClientName = MapTool.createHashMap();
	protected Map<String,String> connectionNameByClientName = MapTool.createHashMap();
	
	
	public BaseParallelClientApp(DataRouter router) {
		super(router);
	}
	
	public BaseParallelClientApp(DataRouter router, Map<String,String> parentConnectionNameByClientName){
		this(router);
		this.parentConnectionNameByClientName = parentConnectionNameByClientName;
	}
	
	/************* app ******************************************************/

	public abstract List<String> getClientNames();
	
	@Override
	public List<Client> getClients() {
		return this.router.getClients(this.getClientNames());
	}
	
	@Override
	public T runInEnvironment(){
		T onceResult = null;
		Collection<T> clientResults = ListTool.createLinkedList();
		Collection<Client> clients = this.getClients();
		try{
			reserveConections();
			onceResult = runOnce();
			for(Client client : CollectionTool.nullSafe(clients)){
				T clientResult = runOncePerClient(client);
				clientResults.add(clientResult);
			}
			releaseConnections();
		}finally{
			this.releaseConnections();
		}
		T mergedResult = mergeResults(onceResult, clientResults);
		return mergedResult;
	}

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
			String parentConnectionName = this.parentConnectionNameByClientName.get(client.getName());
			if(parentConnectionName==null){
				parentConnectionName = this.getClass().getSimpleName();
			}
			String connectionName = connectionClient.reserveConnection(parentConnectionName);
			this.connectionNameByClientName.put(connectionName, client.getName());
		}
	}
	
	@Override
	public void releaseConnections(){
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof ConnectionClient) ){ continue; }
			ConnectionClient connectionClient = (ConnectionClient)client;
			String connectionName = this.connectionNameByClientName.get(client.getName());
			try{
				connectionClient.releaseConnection(connectionName);
			}catch(Exception e){
				logger.warn(ExceptionTool.getStackTraceAsString(e));
				throw new DataAccessException("EXCEPTION THROWN DURING RELEASE OF SINGLE CONNECTION:"
						+connectionName, e);
			}
		}
	}

	
	/********************* config **********************************/
	
	public Config getConfigWithConnectionByName(){
		return new Config().setConnectionNameByClientName(this.connectionNameByClientName);
	}
}
