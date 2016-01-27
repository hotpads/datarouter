package com.hotpads.datarouter.config;

import javax.servlet.ServletContext;

public class ServletContextProvider{

	private final ServletContext servletContext;

	public ServletContextProvider(ServletContext servletContext){
		this.servletContext = servletContext;
	}

	public ServletContext get(){
		return servletContext;
	}

}
