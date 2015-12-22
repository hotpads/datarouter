package com.hotpads.datarouter.util;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;

import com.hotpads.datarouter.config.ServletContextProvider;
import com.hotpads.util.core.concurrent.Lazy;

@Singleton
public class ApplicationPaths{

	private final Lazy<String> path;
	private final Lazy<String> resourcesPath;
	private final Lazy<String> webInfPath;

	@Inject
	public ApplicationPaths(ServletContextProvider servletContextProvider){
		ServletContext servletContext = servletContextProvider.get();
		this.path = Lazy.of(() -> WebappTool.getApplicationRootPath(servletContext));
		this.resourcesPath = Lazy.of(() -> WebappTool.getResourcesPath(servletContext));
		this.webInfPath = Lazy.of(() -> WebappTool.getWebInfPath(servletContext));
	}

	public String getRootPath(){
		return path.get();
	}

	public String getResourcesPath(){
		return resourcesPath.get();
	}

	public String getWebInfPath(){
		return webInfPath.get();
	}

}