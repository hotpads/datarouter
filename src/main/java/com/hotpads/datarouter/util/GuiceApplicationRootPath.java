package com.hotpads.datarouter.util;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuiceApplicationRootPath implements ApplicationRootPath{
	private static final Logger logger = LoggerFactory.getLogger(GuiceApplicationRootPath.class);
	
	private String path;
	private String resourcesPath;
	
	@Inject
	public GuiceApplicationRootPath(@Nullable ServletContext servletContext){
		this.path = WebappTool.getApplicationRootPath(servletContext);// null SC ok
		logger.warn("path:"+path);
		this.resourcesPath = WebappTool.getResourcesPath(servletContext);// null SC ok
		logger.warn("resourcesPath:"+resourcesPath);
	}
	
	@Override
	public String getPath(){
		return path;
	}
	
	@Override
	public String getResourcesPath(){
		return resourcesPath;
	}

}