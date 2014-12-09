package com.hotpads.guice;

import javax.servlet.ServletContext;

import com.google.inject.Injector;
import com.hotpads.DataRouterLoader;
import com.hotpads.WebAppName;

public class GuiceDataRouterLoader extends DataRouterLoader{

	private Injector injector;

	@Override
	protected void init(ServletContext servletContext){
		injector = (Injector)servletContext.getAttribute(Injector.class.getName());
		WebAppName webAppName = injector.getInstance(WebAppName.class);
		webAppName.init(servletContext.getServletContextName());
	}

	@Override
	public <T>T getInstance(Class<? extends T> clazz){
		return injector.getInstance(clazz);
	}

}
