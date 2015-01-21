package com.hotpads;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;

@Singleton
public class WebAppName{

	private String name;
	
	@Inject
	public WebAppName(ServletContext servletContext){
		this.name = servletContext.getServletContextName();
	}

	@Override
	public String toString() {
		return name;
	}

}
