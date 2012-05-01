package com.hotpads.datarouter.routing;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.connection.ConnectionPools;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;


/*
 * DRH=DataRouterHolder, selectively abbreviated because of frequent access
 * 
 * Class for accessing Routers.  Currently supports binding itself to threads.  
 * 
 * Extend this class, add fields for the relevant Router objects, and add a static method createAndBindToThread()
 * 
 * Give each router a static getter like "account()"
 * 
 * Instantiate the DRH and bind it to a thread:
 *   - at the beginning of a web request
 *   - at the start of a job thread
 *   - in the setup of a Test
 *   - at the beginning of a normal application thread
 *   
 *   Then access it in your code by calling the static DRH.asdf()...
 */

public abstract class BaseDRH{

	protected List<ClientId> clientIds;
	protected List<String> clientNames;
	protected ConnectionPools connectionPools;
	protected Clients clients;
	protected Nodes nodes = new Nodes();
	
	protected List<DataRouter> routers = ListTool.createArrayList();
	
	public <R extends DataRouter> R register(R router){
//		router.activate();//caution: make sure nodes are registered before activating
		
		this.routers.add(router);
		return router;
	}
	
	public DataRouter getRouter(String name){
		for(DataRouter router : CollectionTool.nullSafe(this.routers)){
			if(name.equals(router.getName())){
				return router;
			}
		}
		return null;
	}
	
	public List<DataRouter> getRouters(){
		return this.routers;
	}
	
	public List<Client> getClients(){
		SortedSet<Client> clients = SetTool.createTreeSet();
		for(DataRouter router : IterableTool.nullSafe(getRouters())){
			for(Client client : IterableTool.nullSafe(router.getAllClients())){
				clients.add(client);
			}
		}
		return ListTool.createArrayList(clients);
	}
	
	public DataRouter getRouterForClient(Client client){
		for(DataRouter router : routers){
			for(Client c : router.getAllClients()){
				if(c==client){ return router; }
			}
		}
		return null;
	}

	public void clearThreadSpecificState(){
		if(CollectionTool.isEmpty(this.routers)){ return; }
		for(DataRouter router : this.routers){
			router.clearThreadSpecificState();
		}
	}
	
	public List<String> getClientNames(){
		return clientNames;
	}
}
