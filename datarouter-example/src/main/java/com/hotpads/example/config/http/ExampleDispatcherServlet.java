package com.hotpads.example.config.http;

import javax.inject.Singleton;

import com.hotpads.handler.GuiceDispatcherServlet;
import com.hotpads.handler.dispatcher.DatarouterCoreDispatcher;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.job.dispatcher.DatarouterJobDispatcher;
import com.hotpads.notification.config.http.NotificationDispatcher;

@Singleton
@SuppressWarnings("serial")
public class ExampleDispatcherServlet extends GuiceDispatcherServlet{

	@Override
	public void registerDispatchers(){
		dispatchers.add(new DatarouterWebDispatcher(injector, servletContextPath));
		dispatchers.add(new DatarouterJobDispatcher(injector, servletContextPath));
		dispatchers.add(new DatarouterCoreDispatcher(injector, servletContextPath));
		dispatchers.add(new NotificationDispatcher(injector, servletContextPath));
		dispatchers.add(new ExampleDispatcher(injector, servletContextPath));
	}

}