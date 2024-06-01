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
package io.datarouter.graphql.example.loader;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleTeamGraphQlType;
import io.datarouter.graphql.client.util.response.GraphQlResultDto;
import io.datarouter.graphql.example.ExampleQlData;
import io.datarouter.graphql.example.key.ExampleTeamsKey;
import io.datarouter.graphql.loader.DatarouterBatchLoader;
import io.datarouter.scanner.Scanner;
import jakarta.inject.Singleton;

@Singleton
public class ExampleTeamsBatchLoader
implements DatarouterBatchLoader<ExampleTeamsKey,List<ExampleTeamGraphQlType>>{

	@Override
	public Map<ExampleTeamsKey,GraphQlResultDto<List<ExampleTeamGraphQlType>>> load(Set<ExampleTeamsKey> keys){
		return Scanner.of(keys)
				.toMap(Function.identity(), right -> GraphQlResultDto.withData(
						ExampleQlData.orgsToTeams.getOrDefault(right.orgName, List.of())));
	}

}
