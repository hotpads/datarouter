package com.hotpads;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;

@Singleton
public class WebAppName{

	public static final String TEST = "test";

	private String name;

	@Inject
	public WebAppName(@Nullable ServletContext servletContext){
		if(servletContext == null){
			this.name = TEST;
		}else{
			this.name = servletContext.getServletContextName();
		}
	}

	@Override
	public String toString() {
		return name;
	}

}
