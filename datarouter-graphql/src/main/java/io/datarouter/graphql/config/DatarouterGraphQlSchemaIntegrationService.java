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
package io.datarouter.graphql.config;

import io.datarouter.graphql.service.GraphQlInstancesContainer;
import io.datarouter.graphql.service.GraphQlSchemaService;
import io.datarouter.graphql.web.GraphQlBaseHandler;
import io.datarouter.graphql.web.GraphQlFetcherRegistry;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.test.TestableService;
import io.datarouter.util.clazz.AnnotationTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterGraphQlSchemaIntegrationService implements TestableService{

	@Inject
	private GraphQlSchemaService graphQlService;
	@Inject
	private GraphQlInstancesContainer container;
	@Inject
	private DatarouterInjector injector;

	@Override
	public void testAll(){
		container.initializeGraphQlInstances();
		container.getGraphQlHandlers().forEach(this::validateGraphQlHandler);
	}

	private void validateGraphQlHandler(Class<? extends GraphQlBaseHandler> baseHandlerClass){
		graphQlService.build(baseHandlerClass);
		if(container.getForClass(baseHandlerClass) == null){
			throw new IllegalArgumentException("graphql handler handlerClass=" + baseHandlerClass
					+ " was not registered");
		}
		GraphQlBaseHandler handler = injector.getInstance(baseHandlerClass);
		GraphQlFetcherRegistry fetcherRegistry = injector.getInstance(handler.fetcherRegistry());
		fetcherRegistry.scanDataFetcherClasses()
				.forEach(clazz -> AnnotationTool.checkSingletonForClass(clazz, false));
	}

}
