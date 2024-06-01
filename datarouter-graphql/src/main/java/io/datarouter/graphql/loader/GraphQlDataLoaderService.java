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
package io.datarouter.graphql.loader;

import java.util.Map;

import io.datarouter.graphql.config.DatarouterGraphQlExecutors.DataLoaderExecutor;
import io.datarouter.graphql.fetcher.BaseDataLoaderFetcher;
import io.datarouter.graphql.fetcher.DatarouterDataFetcher;
import io.datarouter.graphql.service.GraphQlDataLoaderConfig;
import io.datarouter.graphql.web.GraphQlBaseHandler;
import io.datarouter.graphql.web.GraphQlFetcherRegistry;
import io.datarouter.inject.DatarouterInjector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GraphQlDataLoaderService{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private DataLoaderExecutor executor;

	public GraphQlDataLoaderConfig buildDataloaderConfig(Class<? extends GraphQlBaseHandler> handlerClass){
		GraphQlBaseHandler handler = injector.getInstance(handlerClass);
		GraphQlFetcherRegistry fetcherRegistry = injector.getInstance(handler.fetcherRegistry());
		Map<Class<? extends DatarouterDataFetcher<?,?>>,DatarouterDataLoaderWrapper<? extends DataLoaderKey,?>>
				fetcherClassToLoaders = fetcherRegistry
						.scanDataFetcherClasses()
						.include(BaseDataLoaderFetcher.class::isAssignableFrom)
						.toMap(fetcherClass -> fetcherClass, fetcherClass -> {
							@SuppressWarnings("unchecked")
							DatarouterBatchLoader<?,?> batchedService = buildDataLoaderBatchedServiceInstance(
									(Class<? extends BaseDataLoaderFetcher<?,?,?>>)fetcherClass);
							return DatarouterDataLoaderWrapper.wrap(batchedService, executor);
						});
		return new GraphQlDataLoaderConfig(fetcherClassToLoaders);
	}

	private DatarouterBatchLoader<?,?> buildDataLoaderBatchedServiceInstance(
			Class<? extends BaseDataLoaderFetcher<?,?,?>> fetcherClass){
		BaseDataLoaderFetcher<?,?,?> loader = injector.getInstance(fetcherClass);
		Class<? extends DatarouterBatchLoader<?,?>> batchService = loader.getBatchLoaderClass();
		return injector.getInstance(batchService);
	}

}
