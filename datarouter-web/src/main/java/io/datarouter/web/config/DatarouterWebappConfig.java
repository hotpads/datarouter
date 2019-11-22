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
package io.datarouter.web.config;

import java.util.List;

import javax.servlet.ServletContextListener;

import com.google.inject.Module;

public class DatarouterWebappConfig{

	private final boolean useDatarouterAuth;
	private final List<Module> modules;
	private final List<ServletContextListener> servletContextListeners;

	public DatarouterWebappConfig(
			boolean useDatarouterAuth,
			List<Module> modules,
			List<ServletContextListener> servletContextListeners){
		this.servletContextListeners = servletContextListeners;
		this.modules = modules;
		this.useDatarouterAuth = useDatarouterAuth;
	}

	public boolean getUseDatarouterAuth(){
		return useDatarouterAuth;
	}

	public List<Module> getModules(){
		return modules;
	}

	public List<ServletContextListener> getServletContextListeners(){
		return servletContextListeners;
	}

}
