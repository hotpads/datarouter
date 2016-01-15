package com.hotpads.datarouter.routing;

import java.util.ArrayList;
import java.util.List;

import com.hotpads.datarouter.util.core.DrCollectionTool;


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

@Deprecated//try to replace with combination of Datarouter and Guice
public abstract class BaseDRH{

	protected Datarouter datarouter;
	protected List<Router> routers = new ArrayList<>();

	/************************ constructors ************************/

	protected BaseDRH(Datarouter datarouter){
		this.datarouter = datarouter;
	}

	/************************ methods ************************/

	public <R extends Router> R register(R router){
		this.routers.add(router);
		return router;
	}

	public Router getRouter(String name){
		for(Router router : DrCollectionTool.nullSafe(routers)){
			if(name.trim().equals(router.getName())){
				return router;
			}
		}
		return null;
	}

}
