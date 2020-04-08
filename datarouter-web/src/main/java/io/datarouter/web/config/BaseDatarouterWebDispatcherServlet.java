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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatcherServlet;

@SuppressWarnings("serial")
@Singleton
public class BaseDatarouterWebDispatcherServlet extends DispatcherServlet{

	private final DatarouterInjector injector;

	private final List<Class<? extends BaseRouteSet>> routeSetClasses;
	private final List<BaseRouteSet> routeSets;

	@Inject
	protected BaseDatarouterWebDispatcherServlet(DatarouterInjector injector){
		this.injector = injector;
		this.routeSetClasses = new ArrayList<>();
		this.routeSets = new ArrayList<>();
	}

	protected void addRouteSet(BaseRouteSet routeSet){
		routeSets.add(routeSet);
	}

	@Override
	public void registerRouteSets(){
		register(IterableTool.nullSafeMap(routeSetClasses, injector::getInstance));
		register(routeSets);
	}

}
