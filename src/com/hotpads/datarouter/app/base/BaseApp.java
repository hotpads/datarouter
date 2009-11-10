package com.hotpads.datarouter.app.base;


import org.apache.log4j.Logger;

import com.hotpads.datarouter.app.App;
import com.hotpads.datarouter.routing.DataRouter;

public abstract class BaseApp<T> 
implements App<T> {
	protected Logger logger = Logger.getLogger(this.getClass());

	protected DataRouter router;
	
	public BaseApp(DataRouter router) {
		this.router = router;
	}

	protected abstract T runOnce();

	@Override
	public T runInEnvironment(){
		return runOnce();
	}
	
	
	
}
