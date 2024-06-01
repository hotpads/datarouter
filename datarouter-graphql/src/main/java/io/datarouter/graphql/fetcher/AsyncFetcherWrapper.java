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
package io.datarouter.graphql.fetcher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.datarouter.graphql.config.DatarouterGraphQlSettingsRoot;
import io.datarouter.inject.DatarouterInjector;

public class AsyncFetcherWrapper<T> implements DataFetcher<CompletableFuture<T>>{

	private final Class<? extends DataFetcher<T>> wrappedDataFetcher;
	private final DatarouterGraphQlSettingsRoot graphQlSettings;
	private final Executor executor;
	private final DatarouterInjector injector;

	public static <T> AsyncFetcherWrapper<T> async(
			Class<? extends DataFetcher<T>> wrappedDataFetcher,
			Executor executor,
			DatarouterGraphQlSettingsRoot graphQlSettings,
			DatarouterInjector injector){
		return new AsyncFetcherWrapper<>(wrappedDataFetcher, executor, graphQlSettings, injector);
	}

	private AsyncFetcherWrapper(
			Class<? extends DataFetcher<T>> wrappedDataFetcher,
			Executor executor,
			DatarouterGraphQlSettingsRoot graphQlSettings,
			DatarouterInjector injector){
		this.wrappedDataFetcher = wrappedDataFetcher;
		this.executor = executor;
		this.graphQlSettings = graphQlSettings;
		this.injector = injector;
	}

	@Override
	public CompletableFuture<T> get(DataFetchingEnvironment environment){
		DataFetcher<T> fetcher = injector.getInstance(wrappedDataFetcher);
		if(graphQlSettings.fetchAsynchronously.get()){
			return fetchAsync(environment, fetcher);
		}
		try{
			T data = fetcher.get(environment);
			return CompletableFuture.completedFuture(data);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private CompletableFuture<T> fetchAsync(DataFetchingEnvironment environment, DataFetcher<T> fetcher){
		return CompletableFuture.supplyAsync(() -> {
			try{
				return fetcher.get(environment);
			}catch(Exception e){
				if(e instanceof RuntimeException){
					throw (RuntimeException)e;
				}else{
					throw new RuntimeException(e);
				}
			}
		}, executor);
	}

}
