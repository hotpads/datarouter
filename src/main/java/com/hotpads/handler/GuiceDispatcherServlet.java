package com.hotpads.handler;

import com.google.inject.Injector;
import com.hotpads.DatarouterInjector;
import com.hotpads.guice.GuiceTool;

@SuppressWarnings("serial")
public abstract class GuiceDispatcherServlet extends DispatcherServlet{

	@Override
	protected DatarouterInjector getInjector(){
		Injector injector = GuiceTool.getInjectorFromServletContext(getServletContext());
		return injector.getInstance(DatarouterInjector.class);
	}

}
