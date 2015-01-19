package com.hotpads.guice;

import javax.servlet.ServletContext;

import com.google.inject.Injector;
import com.hotpads.DatarouterLoader;
import com.hotpads.DatarouterInjector;

public class GuiceDatarouterLoader extends DatarouterLoader{

	private Injector injector;

	@Override
	protected void init(ServletContext servletContext){
		this.injector = (Injector)servletContext.getAttribute(Injector.class.getName());
	}

	@Override
	protected DatarouterInjector getInjector(){
		return injector.getInstance(DatarouterInjector.class);
	}

}
