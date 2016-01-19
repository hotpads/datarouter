package com.hotpads.datarouter.inject.guice;

import javax.servlet.ServletContext;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.inject.InjectorRetriever;

public interface GuiceInjectorRetriever extends InjectorRetriever{

	@Override
	default DatarouterInjector getInjector(ServletContext servletContext){
		return GuiceServletTool.getDatarouterInjectorFromServletContext(servletContext);
	}

}
