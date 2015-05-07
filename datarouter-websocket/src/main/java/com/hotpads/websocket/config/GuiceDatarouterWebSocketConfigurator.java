package com.hotpads.websocket.config;

import javax.servlet.ServletContext;

import com.google.inject.Injector;
import com.hotpads.DatarouterInjector;
import com.hotpads.GuiceInjector;
import com.hotpads.guice.GuiceTool;

public class GuiceDatarouterWebSocketConfigurator extends DatarouterWebSocketConfigurator{

	@Override
	protected DatarouterInjector getInjector(ServletContext servletContext){
		Injector injector = GuiceTool.getInjectorFromServletContext(servletContext);
		return new GuiceInjector(injector);
	}

}
