package com.hotpads.websocket.config;

import javax.servlet.ServletContext;

import com.hotpads.DatarouterInjector;
import com.hotpads.guice.GuiceTool;

public class GuiceDatarouterWebSocketConfigurator extends DatarouterWebSocketConfigurator{

	@Override
	protected DatarouterInjector getInjector(ServletContext servletContext){
		return GuiceTool.getDatarouterInjectorFromServletContext(servletContext);
	}

}
