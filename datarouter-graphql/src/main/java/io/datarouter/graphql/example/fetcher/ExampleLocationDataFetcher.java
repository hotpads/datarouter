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
package io.datarouter.graphql.example.fetcher;

import java.util.List;

import io.datarouter.graphql.client.util.example.arg.ExampleLocationGraphQlArgumentType;
import io.datarouter.graphql.client.util.example.type.ExampleLocationGraphQlType;
import io.datarouter.graphql.client.util.response.GraphQlResultDto;
import io.datarouter.graphql.fetcher.BaseDataFetcher;
import io.datarouter.scanner.Scanner;

public class ExampleLocationDataFetcher
extends BaseDataFetcher<List<ExampleLocationGraphQlType>,ExampleLocationGraphQlArgumentType>{

	private static final List<ExampleLocationGraphQlType> LOCATIONS = List.of(
			new ExampleLocationGraphQlType("San Francisco", 250),
			new ExampleLocationGraphQlType("New York", 20),
			new ExampleLocationGraphQlType("Atlanta", 100));

	@Override
	public GraphQlResultDto<List<ExampleLocationGraphQlType>> getData(){
		List<String> queriedBrands = args.locations;
		return Scanner.of(LOCATIONS)
				.include(brand -> queriedBrands.contains(brand.name))
				.listTo(GraphQlResultDto::withData);
	}

}
