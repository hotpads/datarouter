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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import graphql.GraphQL;
import io.datarouter.graphql.client.util.type.GraphQlRootType;
import io.datarouter.graphql.loader.GraphQlDataLoaderService;
import io.datarouter.graphql.playground.GraphQlPlaygroundSampleService;
import io.datarouter.graphql.web.GraphQlBaseHandler;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GraphQlInstancesContainer{

	private static final Map<Class<? extends GraphQlBaseHandler>,GraphQL> classToGraphQl = new HashMap<>();
	private static final Map<Class<? extends GraphQlBaseHandler>,String> graphQlClassToPath = new HashMap<>();
	private static final Map<Class<? extends GraphQlBaseHandler>,Map<GraphQlRootType,Map<String,String>>>
			graphQlClassToSampleQueries = new HashMap<>();
	private static final Map<Class<? extends GraphQlBaseHandler>,GraphQlDataLoaderConfig>
			graphQlClassToDataloaderConfig = new HashMap<>();


	@Inject
	private GraphQlSchemaService graphQlService;
	@Inject
	private GraphQlPlaygroundSampleService sampleService;
	@Inject
	private GraphQlRouteSetRegistry registry;
	@Inject
	private GraphQlDataLoaderService dataLoaderService;

	public void initializeGraphQlInstances(){
		registry.getGraphQlHandlerToPath().entrySet().stream()
				.filter(entry -> GraphQlBaseHandler.class.isAssignableFrom(entry.getKey()))
				.peek(entry -> graphQlClassToPath.put(entry.getKey(), entry
						.getValue()))
				.map(Entry::getKey)
				.peek(handlerClass -> classToGraphQl.put(handlerClass, graphQlService.build(handlerClass)))
				.peek(handlerClass -> graphQlClassToDataloaderConfig.put(handlerClass, dataLoaderService
						.buildDataloaderConfig(handlerClass)))
				.forEach(handlerClass -> graphQlClassToSampleQueries.put(handlerClass, sampleService
						.buildSchemaQuerySamples(handlerClass)));
	}

	public Set<Class<? extends GraphQlBaseHandler>> getGraphQlHandlers(){
		return classToGraphQl.keySet();
	}

	public GraphQL getForClass(Class<? extends GraphQlBaseHandler> graphQlHandler){
		return classToGraphQl.get(graphQlHandler);
	}

	public String getPath(Class<? extends GraphQlBaseHandler> graphQlHandler){
		return graphQlClassToPath.get(graphQlHandler);
	}

	public Map<GraphQlRootType,Map<String,String>> getSchemaSampleQueries(
			Class<? extends GraphQlBaseHandler> graphQlHandler){
		return graphQlClassToSampleQueries.get(graphQlHandler);
	}

	public GraphQlDataLoaderConfig getDataloaderConfig(Class<? extends GraphQlBaseHandler> graphQlHandler){
		return graphQlClassToDataloaderConfig.get(graphQlHandler);
	}

}
