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

import java.util.Map;

import org.dataloader.DataLoaderFactory;
import org.dataloader.DataLoaderRegistry;

import io.datarouter.graphql.fetcher.DatarouterDataFetcher;
import io.datarouter.graphql.loader.DataLoaderKey;
import io.datarouter.graphql.loader.DatarouterDataLoaderWrapper;
import io.datarouter.scanner.Scanner;

public class GraphQlDataLoaderConfig{

	public final Map<Class<? extends DatarouterDataFetcher<?,?>>,DatarouterDataLoaderWrapper<? extends DataLoaderKey,?>>
			fetcherClassToLoader;

	public GraphQlDataLoaderConfig(
			Map<Class<? extends DatarouterDataFetcher<?,?>>,DatarouterDataLoaderWrapper<? extends DataLoaderKey,?>>
			fetcherClassToLoader){
		this.fetcherClassToLoader = fetcherClassToLoader;
	}

	public DataLoaderRegistry buildRegistry(){
		DataLoaderRegistry registry = new DataLoaderRegistry();
		Scanner.of(fetcherClassToLoader.entrySet())
				.forEach(item ->
						registry.register(item.getKey().getSimpleName(),
								DataLoaderFactory.newMappedDataLoader(item.getValue())));
		return registry;
	}

}
