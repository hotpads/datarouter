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

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.dataloader.DataLoaderRegistry;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.datarouter.graphql.client.util.type.GraphQlType;
import io.datarouter.graphql.service.GraphQlDataLoaderConfig;
import io.datarouter.graphql.service.GraphQlInstancesContainer;
import io.datarouter.graphql.util.GraphQlContextKey;
import io.datarouter.graphql.util.GraphQlRequestTool;
import io.datarouter.web.handler.BaseHandler;
import jakarta.inject.Inject;

public abstract class GraphQlBaseHandler extends BaseHandler{

	public static final GraphQlContextKey<HttpServletRequest> HTTP_REQUEST = new GraphQlContextKey<>("httpRequest");

	@Inject
	private GraphQlInstancesContainer container;

	/* create a class that implements GraphQlTypeDto and then use root param of @Ql annotation on each field
	 * to declare as a query or mutation type. See ExampleQl for an example
	 */
	public abstract Class<? extends GraphQlType> getRootType();
	public abstract Class<? extends GraphQlFetcherRegistry> fetcherRegistry();
	public GraphQL graphql;

	@Override
	public void handleWrapper(){
		graphql = container.getForClass(this.getClass());
		super.handleWrapper();
	}

	public ExecutionResult execute(ExecutionInput query){
		return graphql.execute(configureDataLoaders(query));
	}

	private ExecutionInput configureDataLoaders(ExecutionInput query){
		GraphQlDataLoaderConfig config = container.getDataloaderConfig(this.getClass());
		DataLoaderRegistry registry = config.buildRegistry();
		return GraphQlRequestTool.buildExecutionInput(
				query.getQuery(),
				query.getOperationName(),
				query.getVariables(),
				request,
				Optional.ofNullable(registry));
	}

}
