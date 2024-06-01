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

import java.util.concurrent.CompletableFuture;

import org.dataloader.DataLoader;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.datarouter.graphql.client.util.response.GraphQlErrorDto;
import io.datarouter.graphql.client.util.response.GraphQlResultDto;
import io.datarouter.graphql.client.util.type.GraphQlArgumentType;
import io.datarouter.graphql.loader.DataLoaderKey;
import io.datarouter.graphql.loader.DatarouterBatchLoader;
import io.datarouter.graphql.tool.GraphQlTool;
import io.datarouter.web.handler.validator.RequestParamValidator.RequestParamValidatorResponseDto;

public abstract class BaseDataLoaderFetcher<
		T,
		R extends GraphQlArgumentType,
		K extends DataLoaderKey>
extends DatarouterDataFetcher<CompletableFuture<DataFetcherResult<T>>,R>{

	private DataLoader<K,GraphQlResultDto<T>> loader;

	public abstract K buildLoaderKey();
	public abstract Class<? extends DatarouterBatchLoader<K,T>> getBatchLoaderClass();

	@Override
	public CompletableFuture<DataFetcherResult<T>> build(){
		K loaderKey = buildLoaderKey();
		CompletableFuture<GraphQlResultDto<T>> dataResult = loader.load(loaderKey);
		return dataResult.thenApply(result -> GraphQlTool.buildResult(result, environment));
	}

	@Override
	public CompletableFuture<DataFetcherResult<T>> buildInvalidArgResponse(
			RequestParamValidatorResponseDto validationStatus){
		CompletableFuture<GraphQlResultDto<T>> errorResponse = CompletableFuture.completedFuture(GraphQlResultDto
				.withError(GraphQlErrorDto.invalidInput(validationStatus.errorMessage())));
		return errorResponse.thenApply(result -> GraphQlTool.buildResult(result, environment));
	}

	@Override
	public void assignVariables(DataFetchingEnvironment environment){
		super.assignVariables(environment);
		this.loader = environment.getDataLoader(this.getClass().getSimpleName());
	}

	@Override
	public boolean trace(){
		return false;
	}

}
