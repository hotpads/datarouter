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
package io.datarouter.web.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.dispatcher.DispatcherServlet;
import io.datarouter.web.dispatcher.RouteSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class BaseDatarouterWebDispatcherServlet extends DispatcherServlet{

	private final DatarouterInjector injector;
	private final List<Class<RouteSet>> routeSetClasses;
	private final List<RouteSet> routeSets;

	@Inject
	protected BaseDatarouterWebDispatcherServlet(DatarouterInjector injector){
		this.injector = injector;
		this.routeSetClasses = new ArrayList<>();
		this.routeSets = new ArrayList<>();
	}

	protected void addRouteSet(RouteSet routeSet){
		routeSets.add(routeSet);
	}

	@Override
	public void registerRouteSets(){
		Scanner.of(routeSetClasses)
				.map(injector::getInstance)
				.forEach(this::register);
		routeSets.forEach(this::register);
	}

}
