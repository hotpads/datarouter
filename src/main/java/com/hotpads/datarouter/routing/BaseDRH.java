package com.hotpads.datarouter.routing;

import java.util.List;
import java.util.SortedSet;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrSetTool;


/*
 * DRH=DatarouterHolder, selectively abbreviated because of frequent access
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

@Deprecated//try to replace with combination of DatarouterContext and Guice
public abstract class BaseDRH{

	protected DatarouterContext drContext;
	protected List<Datarouter> routers = DrListTool.createArrayList();
	
	
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
		for(Datarouter router : DrCollectionTool.nullSafe(routers)){
			if(name.trim().equals(router.getName())){
				return router;
			}
		}
		return null;
	}
	
	public List<Client> getClients(){
		SortedSet<Client> clients = DrSetTool.createTreeSet();
		for(Datarouter router : DrIterableTool.nullSafe(getRouters())){
			for(Client client : DrIterableTool.nullSafe(router.getAllClients())){
				clients.add(client);
			}
		}
		return DrListTool.createArrayList(clients);
	}
	
	public Datarouter getRouterForClient(Client client){
		for(Datarouter router : routers){
			for(Client c : router.getAllClients()){
				if(c==client){ return router; }
			}
		}
		return null;
	}
	
	/************************** get/set ******************************/
	
	public DatarouterContext getDrContext(){
		return drContext;
	}
	
	public List<Datarouter> getRouters(){
		return this.routers;
	}
}
