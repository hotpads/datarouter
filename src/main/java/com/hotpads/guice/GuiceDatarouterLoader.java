package com.hotpads.guice;

import javax.servlet.ServletContext;

import com.google.inject.Injector;
import com.hotpads.DatarouterLoader;
import com.hotpads.HotPadsWebAppListener;
import com.hotpads.WebAppName;

public class GuiceDatarouterLoader extends DatarouterLoader{

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
