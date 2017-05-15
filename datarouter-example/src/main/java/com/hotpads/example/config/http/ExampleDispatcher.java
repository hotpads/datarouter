package com.hotpads.example.config.http;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.example.SystemApiHandler;
import com.hotpads.example.handler.DatarouterExampleDefaultHandler;
import com.hotpads.handler.dispatcher.BaseDispatcherRoutes;

public class ExampleDispatcher extends BaseDispatcherRoutes{

	public ExampleDispatcher(DatarouterInjector injector, String servletContextPath){
		super(injector, servletContextPath, "");
		handleDir("/system").withHandler(SystemApiHandler.class);
		handle("|/").withHandler(DatarouterExampleDefaultHandler.class);
	}

}
