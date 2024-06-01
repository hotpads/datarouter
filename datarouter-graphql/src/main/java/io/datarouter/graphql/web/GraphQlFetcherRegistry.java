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
package io.datarouter.graphql.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.datarouter.graphql.fetcher.DatarouterDataFetcher;
import io.datarouter.scanner.Scanner;

public abstract class GraphQlFetcherRegistry{

	private final Map<String,Class<? extends DatarouterDataFetcher<?,?>>> fetcherIdToDataFetcherClassMap;

	public GraphQlFetcherRegistry(){
		fetcherIdToDataFetcherClassMap = new HashMap<>();
	}

	public void bind(String name, Class<? extends DatarouterDataFetcher<?,?>> fetcherClass){
		fetcherIdToDataFetcherClassMap.put(name, fetcherClass);
	}

	public Optional<Class<? extends DatarouterDataFetcher<?,?>>> find(String name){
		return Optional.ofNullable(fetcherIdToDataFetcherClassMap.get(name));
	}

	public Scanner<Class<? extends DatarouterDataFetcher<?,?>>> scanDataFetcherClasses(){
		return Scanner.of(fetcherIdToDataFetcherClassMap.values());
	}

}
