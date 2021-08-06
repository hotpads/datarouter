/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.web.app;

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
		return fsAbsolutePath.substring(0, fsAbsolutePath.length() - slugFileName.length() - 1);
	}

	public static String getResourcesPath(ServletContext servletContext){
		String rootPath = getApplicationRootPath(servletContext);
		String classesPath = rootPath + "/WEB-INF/classes";
		if(new File(classesPath).exists()){// packaged webapp
			return classesPath;
		}
		String srcMainResourcesPath = rootPath + "/src/main/resources";
		if(new File(srcMainResourcesPath).exists()){// for tests
			return srcMainResourcesPath;
		}
		throw new RuntimeException("can't find any resources");
	}


	public static String getWebInfPath(ServletContext servletContext){
		String rootPath = getApplicationRootPath(servletContext);
		String classesPath = rootPath + "/WEB-INF";
		if(new File(classesPath).exists()){// packaged webapp
			return classesPath;
		}
		String srcMainResourcesPath = rootPath + "/src/main/webapp/WEB-INF";
		if(new File(srcMainResourcesPath).exists()){// for tests
			return srcMainResourcesPath;
		}
		throw new RuntimeException("can't find WEB-INF folder");
	}

}
