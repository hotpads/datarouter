package com.hotpads.datarouter.inject.guice;

import javax.servlet.ServletContext;

import com.google.inject.Injector;
import com.hotpads.datarouter.inject.DatarouterInjector;

public class GuiceServletTool{

	public static DatarouterInjector getDatarouterInjectorFromServletContext(ServletContext servletContext){
		Injector injector = (Injector)servletContext.getAttribute(Injector.class.getName());
		return injector.getInstance(DatarouterInjector.class);
	}

}
