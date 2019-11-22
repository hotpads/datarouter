/**
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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;

import io.datarouter.web.config.ServletContextSupplier;

@Singleton
public class WebappName{

	public static final String TEST = "test";

	private final String name;

	@Inject
	public WebappName(ServletContextSupplier servletContextSupplier){
		ServletContext servletContext = servletContextSupplier.get();
		if(servletContext == null){
			this.name = TEST;
		}else{
			this.name = servletContext.getServletContextName();
		}
	}

	@Override
	public String toString(){
		return name;
	}

	/**
	 * @return the content of the display-name tag in the web.xml
	 */
	public String getName(){
		return name;
	}

}
