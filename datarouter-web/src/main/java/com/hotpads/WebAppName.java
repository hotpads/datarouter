package com.hotpads;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;

import com.hotpads.datarouter.config.ServletContextProvider;

@Singleton
public class WebAppName{

	public static final String TEST = "test";

	//note: this seems to be the artifactId in the pom (not the directory that the pom is in)
	private String name;

	@Inject
	public WebAppName(ServletContextProvider servletContextProvider){
		ServletContext servletContext = servletContextProvider.get();
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

	public String getName(){
		return name;
	}

}
