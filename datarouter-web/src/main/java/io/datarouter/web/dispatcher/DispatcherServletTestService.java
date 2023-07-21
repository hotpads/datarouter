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
package io.datarouter.web.dispatcher;

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import io.datarouter.inject.DatarouterInjector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DispatcherServletTestService{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private DispatcherServletClasses dispatcherServletClasses;

	public void testHandlerInjection(ServletConfig servletConfig){
		dispatcherServletClasses.get().stream()
				.map(injector::getInstance)
				.forEach(servlet -> testSingleDispatcher(servletConfig, servlet));
	}

	private void testSingleDispatcher(ServletConfig servletConfig, DispatcherServlet servlet){
		try{
			servlet.init(servletConfig);
		}catch(ServletException e){
			throw new RuntimeException(e);
		}
		servlet.getRouteSets().stream()
				.map(RouteSet::getDispatchRules)
				.flatMap(List::stream)
				.map(DispatchRule::getHandlerClass)
				.distinct()
				.filter(handler -> !NonEagerInitHandler.class.isAssignableFrom(handler))
				.forEach(injector::getInstance);
	}

}
