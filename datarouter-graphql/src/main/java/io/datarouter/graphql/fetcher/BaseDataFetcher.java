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

import graphql.execution.DataFetcherResult;
import io.datarouter.graphql.client.util.response.GraphQlErrorDto;
import io.datarouter.graphql.client.util.response.GraphQlResultDto;
import io.datarouter.graphql.client.util.type.GraphQlArgumentType;
import io.datarouter.graphql.tool.GraphQlTool;
import io.datarouter.web.handler.validator.RequestParamValidator.RequestParamValidatorResponseDto;

public abstract class BaseDataFetcher<T,R extends GraphQlArgumentType>
extends DatarouterDataFetcher<DataFetcherResult<T>,R>{

	public abstract GraphQlResultDto<T> getData();

	@Override
	public DataFetcherResult<T> build(){
		GraphQlResultDto<T> dataResponse = getData();
		return GraphQlTool.buildResult(dataResponse, environment);
	}

	@Override
	public DataFetcherResult<T> buildInvalidArgResponse(
			RequestParamValidatorResponseDto validationStatus){
		GraphQlResultDto<T> result = GraphQlResultDto
				.withError(GraphQlErrorDto.invalidInput(validationStatus.errorMessage()));
		return GraphQlTool.buildResult(result, environment);
	}

	@Override
	public boolean trace(){
		return true;
	}

}
