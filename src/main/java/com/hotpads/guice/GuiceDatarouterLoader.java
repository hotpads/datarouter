package com.hotpads.guice;

import javax.servlet.ServletContext;

import com.google.inject.Injector;
import com.hotpads.DatarouterInjector;
import com.hotpads.DatarouterLoader;

public class GuiceDatarouterLoader extends DatarouterLoader{

	private Injector injector;

	@Override
	protected void init(ServletContext servletContext){
		this.injector = GuiceTool.getInjectorFromServletContext(servletContext);
	}

	@Override
	protected DatarouterInjector getInjector(){
		return injector.getInstance(DatarouterInjector.class);
	}

}
