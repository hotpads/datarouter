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
package io.datarouter.graphql.example;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import io.datarouter.graphql.client.util.example.type.ExampleGraphQlRootType;
import io.datarouter.graphql.client.util.type.GraphQlType;
import io.datarouter.graphql.example.fetcher.registry.ExampleGraphQlFetcherRegistry;
import io.datarouter.graphql.web.GraphQlBaseHandler;
import io.datarouter.graphql.web.GraphQlFetcherRegistry;
import io.datarouter.httpclient.endpoint.param.RequestBody;

public class ExampleGraphQlHandler extends GraphQlBaseHandler{
//TODO move ExampleGraphQlHandler and dependent classes to test package after it is no longer needed for testing

	@Handler
	public ExecutionResult example(@RequestBody ExecutionInput query){
		return execute(query);
	}

	@Override
	public Class<? extends GraphQlType> getRootType(){
		return ExampleGraphQlRootType.class;
	}

	@Override
	public Class<? extends GraphQlFetcherRegistry> fetcherRegistry(){
		return ExampleGraphQlFetcherRegistry.class;
	}

}
