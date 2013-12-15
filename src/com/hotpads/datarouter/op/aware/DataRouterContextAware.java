package com.hotpads.datarouter.op.aware;

import com.hotpads.datarouter.routing.DataRouterContext;

public interface DataRouterContextAware<T>{

	DataRouterContext getDataRouterContext();
	
}
