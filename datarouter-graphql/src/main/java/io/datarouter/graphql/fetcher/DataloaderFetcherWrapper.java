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

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.datarouter.inject.DatarouterInjector;

public class DataloaderFetcherWrapper<T> implements DataFetcher<T>{

	private final Class<? extends DataFetcher<T>> wrappedDataFetcher;
	private final DatarouterInjector injector;

	public static <T> DataloaderFetcherWrapper<T> wrap(
			Class<? extends DataFetcher<T>> wrappedDataFetcher,
			DatarouterInjector injector){
		return new DataloaderFetcherWrapper<>(wrappedDataFetcher, injector);
	}

	private DataloaderFetcherWrapper(
			Class<? extends DataFetcher<T>> wrappedDataFetcher,
			DatarouterInjector injector){
		this.wrappedDataFetcher = wrappedDataFetcher;
		this.injector = injector;
	}

	@Override
	public T get(DataFetchingEnvironment environment) throws Exception{
		DataFetcher<T> fetcher = injector.getInstance(wrappedDataFetcher);
		return fetcher.get(environment);
	}

}
