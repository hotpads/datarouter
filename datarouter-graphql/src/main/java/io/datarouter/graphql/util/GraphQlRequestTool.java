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
package io.datarouter.graphql.util;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.dataloader.DataLoaderRegistry;

import graphql.ExecutionInput;
import graphql.GraphQLContext;
import graphql.execution.ExecutionId;
import io.datarouter.graphql.web.GraphQlBaseHandler;
import io.datarouter.gson.DatarouterGsons;
import io.datarouter.web.util.http.RequestTool;

public class GraphQlRequestTool{

	@SuppressWarnings("deprecation")
	public static ExecutionInput getApolloFormat(HttpServletRequest request){
		String body = RequestTool.getBodyAsString(request);
		try{
			ExecutionInputDto executionInput = DatarouterGsons.withUnregisteredEnums().fromJson(
					body,
					ExecutionInputDto.class);
			return buildExecutionInput(
					executionInput.query,
					executionInput.operationName,
					executionInput.variables,
					request,
					Optional.empty());
		}catch(Exception e){
			GraphQlCounters.inc("raw string query");
			return buildExecutionInput(body, null, null, request, Optional.empty());
		}
	}

	public static ExecutionInput buildExecutionInput(
			String query,
			String operationName,
			Map<String,Object> variables,
			HttpServletRequest request,
			Optional<DataLoaderRegistry> registry){
		ExecutionInput.Builder executionInputBuilder = ExecutionInput.newExecutionInput();
		if(query != null){
			executionInputBuilder = executionInputBuilder.query(query);
		}
		if(operationName != null){
			executionInputBuilder = executionInputBuilder.operationName(operationName);
		}
		if(variables != null){
			executionInputBuilder = executionInputBuilder.variables(variables);
		}
		GraphQLContext.Builder context = GraphQLContext.newContext().of(GraphQlBaseHandler.HTTP_REQUEST.name, request);
		return executionInputBuilder
				.executionId(ExecutionId.generate())
				.context(context)
				.dataLoaderRegistry(registry.orElse(new DataLoaderRegistry()))
				.build();
	}

	private static class ExecutionInputDto{

		public String query;
		public String operationName;
		public Map<String, Object> variables;

	}

}
