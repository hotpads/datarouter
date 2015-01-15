package com.hotpads.datarouter.op.aware;

import com.hotpads.datarouter.routing.DatarouterContext;

public interface DataRouterContextAware<T>{

	DatarouterContext getDataRouterContext();
	
}
