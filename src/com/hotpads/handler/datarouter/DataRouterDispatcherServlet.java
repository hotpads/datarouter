package com.hotpads.handler.datarouter;

import com.google.inject.Singleton;
import com.hotpads.handler.DispatcherServlet;
import com.hotpads.handler.datarouter.dispatcher.RootDispatcher;

@SuppressWarnings("serial")
@Singleton
public class DataRouterDispatcherServlet extends DispatcherServlet{

	@Override
	public void registerDispatchers(){
//		dispatchers.add(new AdminDispatcher(injector, servletContextPath, "/admin"));
		dispatchers.add(new RootDispatcher(injector, servletContextPath, ""));
	}
	
}
