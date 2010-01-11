package com.hotpads.datarouter.routing;

import java.util.List;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;


/*
 * DRF=DataRouterFinder, selectively abbreviated because of frequent access
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
 *   Then access it in your code by calling the static DRF.asdf()...
 */

public abstract class BaseDRH{
	
	protected List<DataRouter> routers = ListTool.createArrayList();
	
	public <R extends DataRouter> R register(R router){
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

	public void clearThreadSpecificState(){
		if(CollectionTool.isEmpty(this.routers)){ return; }
		for(DataRouter router : this.routers){
			router.clearThreadSpecificState();
		}
	}
}
