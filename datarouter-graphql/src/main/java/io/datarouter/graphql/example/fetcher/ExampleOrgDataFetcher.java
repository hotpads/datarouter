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

import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleOrgGraphQlType;
import io.datarouter.graphql.client.util.response.GraphQlResultDto;
import io.datarouter.graphql.example.ExampleQlData;
import io.datarouter.graphql.fetcher.BaseDataFetcher;
import io.datarouter.graphql.service.GraphQlSchemaService.EmptyGraphQlArgumentType;

public class ExampleOrgDataFetcher
extends BaseDataFetcher<List<ExampleOrgGraphQlType>,EmptyGraphQlArgumentType>{

	@Override
	public GraphQlResultDto<List<ExampleOrgGraphQlType>> getData(){
		return GraphQlResultDto.withData(ExampleQlData.orgs);
	}

}
