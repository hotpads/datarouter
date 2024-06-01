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

import io.datarouter.graphql.client.util.example.arg.ExampleOfficeGraphQlArgumentType;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType;
import io.datarouter.graphql.client.util.response.GraphQlErrorDto;
import io.datarouter.graphql.client.util.response.GraphQlResultDto;
import io.datarouter.graphql.example.ExampleQlData;
import io.datarouter.graphql.fetcher.BaseDataFetcher;

public class ExampleOfficeDataFetcher
extends BaseDataFetcher<ExampleOfficeGraphQlType,ExampleOfficeGraphQlArgumentType>{

	@Override
	public GraphQlResultDto<ExampleOfficeGraphQlType> getData(){
		String location = args.location;
		if(location == null){
			return GraphQlResultDto.withError(GraphQlErrorDto.invalidInput("location cannot be null!"));
		}
		return GraphQlResultDto.withData(ExampleQlData.placeToOffice.get(location));
	}

}
