package com.hotpads.datarouter.inject;

import javax.servlet.ServletContext;

public interface InjectorRetriever{

	DatarouterInjector getInjector(ServletContext servletContext);

}
