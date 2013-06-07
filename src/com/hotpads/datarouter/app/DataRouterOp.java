package com.hotpads.datarouter.app;

import java.util.concurrent.Callable;

import com.hotpads.datarouter.routing.DataRouterContext;

public interface DataRouterOp<T>
extends Callable<T>{

	DataRouterContext getDataRouterContext();
	
}
