package com.hotpads.datarouter.op.aware;

import com.hotpads.datarouter.routing.DatarouterContext;

public interface DatarouterContextAware<T>{

	DatarouterContext getDataRouterContext();
	
}
