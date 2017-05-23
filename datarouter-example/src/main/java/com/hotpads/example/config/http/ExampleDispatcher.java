package com.hotpads.example.config.http;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.example.SystemApiHandler;
import com.hotpads.example.handler.DatarouterExampleDefaultHandler;
import com.hotpads.handler.dispatcher.BaseRouteSet;

public class ExampleDispatcher extends BaseRouteSet{

	public ExampleDispatcher(DatarouterInjector injector, String servletContextPath){
		super(injector, servletContextPath, "");
		handleDir("/system").withHandler(SystemApiHandler.class);
		handle("|/").withHandler(DatarouterExampleDefaultHandler.class);
	}

}
