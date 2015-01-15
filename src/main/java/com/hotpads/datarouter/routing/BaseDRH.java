package com.hotpads.datarouter.routing;

import java.util.List;
import java.util.SortedSet;

import com.hotpads.datarouter.client.Client;
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

@Deprecated//try to replace with combination of DataRouterContext and Guice
public abstract class BaseDRH{

	protected DatarouterContext drContext;
	protected List<Datarouter> routers = ListTool.createArrayList();
	
	
	/************************ constructors ************************/
	
	protected BaseDRH(DatarouterContext drContext){
		this.drContext = drContext;
	}
	
	
	/************************ methods ************************/
	
	public <R extends Datarouter> R register(R router){
//		router.activate();//caution: make sure nodes are registered before activating
		this.routers.add(router);
		return router;
	}
	
	public Datarouter getRouter(String name){
		for(Datarouter router : CollectionTool.nullSafe(routers)){
			if(name.trim().equals(router.getName())){
				return router;
			}
		}
		return null;
	}
	
	public List<Client> getClients(){
		SortedSet<Client> clients = SetTool.createTreeSet();
		for(Datarouter router : IterableTool.nullSafe(getRouters())){
			for(Client client : IterableTool.nullSafe(router.getAllClients())){
				clients.add(client);
			}
		}
		return ListTool.createArrayList(clients);
	}
	
	public Datarouter getRouterForClient(Client client){
		for(Datarouter router : routers){
			for(Client c : router.getAllClients()){
				if(c==client){ return router; }
			}
		}
		return null;
	}

	public void clearThreadSpecificState(){
		if(CollectionTool.isEmpty(this.routers)){ return; }
		for(Datarouter router : this.routers){
			router.clearThreadSpecificState();
		}
	}
	
	
	/************************** get/set ******************************/
	
	public DatarouterContext getDrContext(){
		return drContext;
	}
	
	public List<Datarouter> getRouters(){
		return this.routers;
	}
}
