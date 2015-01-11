package com.hotpads.datarouter.util;

import java.io.File;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebappTool{
	private static final Logger logger = LoggerFactory.getLogger(WebappTool.class);
	
	public static String getApplicationRootPath(ServletContext servletContext){
		String slugFileName = "slug";
		
		String fsAbsolutePath;
		if(servletContext == null){
			fsAbsolutePath = new File(slugFileName).getAbsolutePath();
//			logger.warn("without ServletContext: "+fsAbsolutePath);
		}else{
			fsAbsolutePath = servletContext.getRealPath(slugFileName);
//			logger.warn("with ServletContext: "+fsAbsolutePath);
		}
		
		//omit trailing slash
		String fsAppRoot = fsAbsolutePath.substring(0, fsAbsolutePath.length() - slugFileName.length() - 1);
		return fsAppRoot;
	}
	
	public static String getResourcesPath(ServletContext servletContext){
		String rootPath = getApplicationRootPath(servletContext);
		String classesPath = rootPath + "/WEB-INF/classes";
		String srcMainResourcesPath = rootPath + "/src/main/resources";
		if(new File(classesPath).exists()){//packaged webapp
			return classesPath;
		}else if(new File(srcMainResourcesPath).exists()){//for tests
			return srcMainResourcesPath;
		}
		throw new RuntimeException("can't find any resources");
	}
	
}
