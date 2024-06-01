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
package io.datarouter.graphql.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.datarouter.graphql.config.DatarouterGraphQlRouteSet;
import io.datarouter.graphql.web.GraphQlBaseHandler;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ClassTool;
import io.datarouter.web.config.RouteSetRegistry;
import io.datarouter.web.dispatcher.RouteSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GraphQlRouteSetRegistry{

	private final Map<Class<? extends GraphQlBaseHandler>,String> graphQlHandlerToPath;

	@Inject
	public GraphQlRouteSetRegistry(DatarouterInjector datarouterInjector, RouteSetRegistry routeSetRegistry){
		graphQlHandlerToPath = new HashMap<>();
		//TODO delete the line below after moving ExampleGraphQlHandler to test package
		registerGraphQlRouteSets(List.of(datarouterInjector.getInstance(DatarouterGraphQlRouteSet.class)));
		registerGraphQlRouteSets(routeSetRegistry.get());
	}


	private void registerGraphQlRouteSets(List<RouteSet> routeSets){
		Scanner.of(routeSets)
			.concatIter(RouteSet::getDispatchRulesNoRedirects)
			.forEach(rule -> ClassTool.castIfPossible(GraphQlBaseHandler.class, rule.getHandlerClass())
					.ifPresent(handlerClass -> graphQlHandlerToPath.put(handlerClass, rule.getRegex())));
	}

	public Map<Class<? extends GraphQlBaseHandler>,String> getGraphQlHandlerToPath(){
		return graphQlHandlerToPath;
	}

}
