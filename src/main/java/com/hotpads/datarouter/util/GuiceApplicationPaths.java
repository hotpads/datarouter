package com.hotpads.datarouter.util;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuiceApplicationPaths implements ApplicationPaths{
	private static final Logger logger = LoggerFactory.getLogger(GuiceApplicationPaths.class);
	
	private String path;
	private String resourcesPath;
	
	@Inject
	public GuiceApplicationPaths(@Nullable ServletContext servletContext){
		this.path = WebappTool.getApplicationRootPath(servletContext);// null SC ok
		logger.warn("path:"+path);
		this.resourcesPath = WebappTool.getResourcesPath(servletContext);// null SC ok
		logger.warn("resourcesPath:"+resourcesPath);
	}
	
	@Override
	public String getRootPath(){
		return path;
	}
	
	@Override
	public String getResourcesPath(){
		return resourcesPath;
	}

}