package com.hotpads.datarouter.routing;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import com.hotpads.datarouter.client.Client;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
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
 * Instantiate the DRF and bind it to a thread:
 *   - at the beginning of a web request
 *   - at the start of a job thread
 *   - in the setup of a Test
 *   - at the beginning of a normal application thread
 *   
 *   Then access it in your code by calling the static DRH.asdf()...
 */

public abstract class BaseDRH{
	
	protected List<DataRouter> routers = ListTool.createArrayList();
	protected Map<Client,DataRouter> routerByClient = MapTool.createHashMap();
	
	public <R extends DataRouter> R register(R router) throws IOException{
//		router.activate();//caution: make sure nodes are registered before activating
		this.routers.add(router);
		for(Client client : router.getAllClients()){
			routerByClient.put(client, router);
		}
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
		return routerByClient.get(client);
	}

	public void clearThreadSpecificState(){
		if(CollectionTool.isEmpty(this.routers)){ return; }
		for(DataRouter router : this.routers){
			router.clearThreadSpecificState();
		}
	}
}
