package com.hotpads.guice;

import javax.servlet.ServletContext;

import com.google.inject.Injector;
import com.hotpads.DataRouterLoader;
import com.hotpads.HotPadsWebAppListener;

public class GuiceDataRouterLoader extends DataRouterLoader{

	private Injector injector;

	@Override
	protected void init(ServletContext servletContext){
		injector = (Injector)servletContext.getAttribute(Injector.class.getName());
	}

	@Override
	protected HotPadsWebAppListener buildListener(Class<? extends HotPadsWebAppListener> listenerClass){
		return injector.getInstance(listenerClass);
	}

}
