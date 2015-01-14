package com.hotpads.guice;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.google.inject.Injector;
import com.hotpads.DataRouterLoader;
import com.hotpads.HotPadsWebAppListener;
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
	protected HotPadsWebAppListener buildListener(Class<? extends HotPadsWebAppListener> listenerClass){
		return injector.getInstance(listenerClass);
	}

}
