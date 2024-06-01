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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.graphql.client.util.example.arg.ExampleOfficeGraphQlArgumentType.ExampleSnackGraphQlArgumentType;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleFloorGraphQlType;
import io.datarouter.graphql.client.util.example.type.ExampleOfficeGraphQlType.ExampleSnackGraphQlType;
import io.datarouter.graphql.client.util.response.GraphQlErrorDto;
import io.datarouter.graphql.client.util.response.GraphQlResultDto;
import io.datarouter.graphql.fetcher.BaseDataFetcher;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.handler.validator.IntegerRequestParamValidator;
import io.datarouter.web.handler.validator.RequestParamValidator;

public class ExampleSnackDataFetcher
extends BaseDataFetcher<List<ExampleSnackGraphQlType>,ExampleSnackGraphQlArgumentType>{

	private static final Map<Integer,List<ExampleSnackGraphQlType>> floorToSnacks = Map.of(
			10,
			List.of(
					new ExampleSnackGraphQlType("beef jerky", 10),
					new ExampleSnackGraphQlType("chips", 17),
					new ExampleSnackGraphQlType("la croix", 1),
					new ExampleSnackGraphQlType("seaweed", 0),
					new ExampleSnackGraphQlType("bananas", 5)));

	@Override
	public GraphQlResultDto<List<ExampleSnackGraphQlType>> getData(){
		ExampleFloorGraphQlType dto = environment.getSource();
		List<ExampleSnackGraphQlType> snacks = floorToSnacks.get(dto.floorNum);
		if(snacks == null){
			return GraphQlResultDto.withError(GraphQlErrorDto.invalidInput("no snack found on floor " + dto.floorNum));
		}
		return Scanner.of(snacks)
				.limit(args.limit)
				.listTo(GraphQlResultDto::withData);
	}

	@Override
	public Map<Class<? extends RequestParamValidator<?>>,Object> argumentsToValidators(){
		return Map.of(ExampleLimitParamValidator.class, args.limit);
	}

	public static class ExampleLimitParamValidator extends IntegerRequestParamValidator{

		@Override
		public RequestParamValidatorResponseDto validate(HttpServletRequest request, Integer limit){
			if(limit <= 0){
				return RequestParamValidatorResponseDto.makeErrorResponse("limit has to be larger than 0.");
			}
			return RequestParamValidatorResponseDto.makeSuccessResponse();
		}

	}

}
