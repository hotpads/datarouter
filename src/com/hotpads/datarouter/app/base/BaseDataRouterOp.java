package com.hotpads.datarouter.app.base;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.app.DataRouterOp;
import com.hotpads.datarouter.routing.DataRouterContext;

public abstract class BaseDataRouterOp<T>
implements DataRouterOp<T>{

	private Logger logger = Logger.getLogger(getClass());
	private DataRouterContext drContext;

	public BaseDataRouterOp(DataRouterContext drContext){
		this.drContext = drContext;
	}

	public DataRouterContext getDataRouterContext(){
		return drContext;
	}

	public Logger getLogger(){
		return logger;
	}
	
}
