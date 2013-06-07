package com.hotpads.datarouter.app.base;


import com.hotpads.datarouter.app.RouterOp;
import com.hotpads.datarouter.routing.DataRouter;

@Deprecated//is this used?
public abstract class BaseRouterOp<T> 
implements RouterOp<T> {
	
	private DataRouter router;
	
	public BaseRouterOp(DataRouter router) {
		this.router = router;
	}

	public DataRouter getRouter(){
		return router;
	}
	
}
