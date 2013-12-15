package com.hotpads.datarouter.op;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.op.aware.DataRouterContextAware;
import com.hotpads.datarouter.routing.DataRouterContext;

public abstract class BaseDataRouterOp<T>
implements DataRouterContextAware<T>{

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
