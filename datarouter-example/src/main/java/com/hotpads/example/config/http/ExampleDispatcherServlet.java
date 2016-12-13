package com.hotpads.example.config.http;

import javax.inject.Singleton;

import com.hotpads.handler.dispatcher.DatarouterCoreDispatcher;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.handler.dispatcher.GuiceDispatcherServlet;
import com.hotpads.job.dispatcher.DatarouterJobDispatcher;

@Singleton
@SuppressWarnings("serial")
public class ExampleDispatcherServlet extends GuiceDispatcherServlet{

	@Override
	public void registerDispatchers(){
		dispatchers.add(new DatarouterWebDispatcher(injector, servletContextPath));
		dispatchers.add(new DatarouterJobDispatcher(injector, servletContextPath));
		dispatchers.add(new DatarouterCoreDispatcher(injector, servletContextPath));
		dispatchers.add(new ExampleDispatcher(injector, servletContextPath));
	}

}