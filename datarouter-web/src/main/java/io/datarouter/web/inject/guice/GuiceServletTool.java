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
package io.datarouter.web.inject.guice;

import javax.servlet.ServletContext;

import com.google.inject.Injector;

import io.datarouter.inject.DatarouterInjector;

public class GuiceServletTool{

	public static DatarouterInjector getDatarouterInjectorFromServletContext(ServletContext servletContext){
		Injector injector = (Injector)servletContext.getAttribute(Injector.class.getName());
		return injector.getInstance(DatarouterInjector.class);
	}

}
