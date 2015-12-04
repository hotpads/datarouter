package com.hotpads.example.config.http;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.example.SystemApiHandler;
import com.hotpads.handler.BaseDispatcher;

public class ExampleDispatcher extends BaseDispatcher{

	public ExampleDispatcher(DatarouterInjector injector, String servletContextPath){
		super(injector, servletContextPath, "");
		handleDir("/system").withHandler(SystemApiHandler.class);
	}

}
