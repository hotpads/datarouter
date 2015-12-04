package com.hotpads.example.config.http;

import javax.inject.Singleton;

import com.hotpads.handler.GuiceDispatcherServlet;

@Singleton
@SuppressWarnings("serial")
public class ExampleDispatcherServlet extends GuiceDispatcherServlet{

	@Override
	public void registerDispatchers(){
		dispatchers.add(new ExampleDispatcher(injector, servletContextPath));
	}

}