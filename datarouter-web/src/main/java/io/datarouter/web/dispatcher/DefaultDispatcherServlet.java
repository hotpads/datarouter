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

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.web.config.BaseDatarouterWebDispatcherServlet;
import io.datarouter.web.config.RouteSetRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class DefaultDispatcherServlet extends BaseDatarouterWebDispatcherServlet{

	@Inject
	public DefaultDispatcherServlet(DatarouterInjector injector, RouteSetRegistry routeSetRegistry){
		super(injector);
		routeSetRegistry.get().forEach(this::addRouteSet);
	}

}
