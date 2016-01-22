package com.hotpads.datarouter.util;

import java.io.File;

import javax.servlet.ServletContext;

public class WebappTool{

	public static String getApplicationRootPath(ServletContext servletContext){
		String slugFileName = "WEB-INF/web.xml";

		String fsAbsolutePath;
		if(servletContext == null){
			fsAbsolutePath = new File(slugFileName).getAbsolutePath();
		}else{
			fsAbsolutePath = servletContext.getRealPath(slugFileName);
		}

		// omit trailing slash
		String fsAppRoot = fsAbsolutePath.substring(0, fsAbsolutePath.length() - slugFileName.length() - 1);
		return fsAppRoot;
	}

	public static String getResourcesPath(ServletContext servletContext){
		String rootPath = getApplicationRootPath(servletContext);
		String classesPath = rootPath + "/WEB-INF/classes";
		String srcMainResourcesPath = rootPath + "/src/main/resources";
		if(new File(classesPath).exists()){// packaged webapp
			return classesPath;
		}else if(new File(srcMainResourcesPath).exists()){// for tests
			return srcMainResourcesPath;
		}
		throw new RuntimeException("can't find any resources");
	}


	public static String getWebInfPath(ServletContext servletContext){
		String rootPath = getApplicationRootPath(servletContext);
		String classesPath = rootPath + "/WEB-INF";
		String srcMainResourcesPath = rootPath + "/src/main/webapp/WEB-INF";
		if(new File(classesPath).exists()){// packaged webapp
			return classesPath;
		}else if(new File(srcMainResourcesPath).exists()){// for tests
			return srcMainResourcesPath;
		}
		throw new RuntimeException("can't find WEB-INF folder");
	}

}
