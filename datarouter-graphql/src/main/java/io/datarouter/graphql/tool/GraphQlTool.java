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
package io.datarouter.graphql.tool;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.datarouter.graphql.client.util.config.Ql;
import io.datarouter.graphql.client.util.response.GraphQlErrorDto;
import io.datarouter.graphql.client.util.response.GraphQlResultDto;
import io.datarouter.graphql.client.util.type.GraphQlArgumentType;
import io.datarouter.graphql.error.DatarouterGraphQlDataValidationError;
import io.datarouter.graphql.fetcher.DatarouterDataFetcher;
import io.datarouter.util.string.StringTool;

public class GraphQlTool{

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static Class<? extends GraphQlArgumentType> getArgumentClassFromFetcherClass(
			Class<? extends DatarouterDataFetcher> fetcherClass){
		return (Class<? extends GraphQlArgumentType>)((ParameterizedType)fetcherClass.getGenericSuperclass())
				.getActualTypeArguments()[1];
	}

	public static String getDescriptionFromField(Field field){
		if(field.isAnnotationPresent(Ql.class)){
			String description = field.getAnnotation(Ql.class).description();
			return StringTool.isEmptyOrWhitespace(description) ? null : description;
		}
		return null;
	}

	public static boolean fieldIsRequired(Field field){
		if(field.isAnnotationPresent(Ql.class)){
			return field.getAnnotation(Ql.class).required();
		}
		return false;
	}

	public static <T> DataFetcherResult<T> buildResult(GraphQlResultDto<T> result, DataFetchingEnvironment environment){
		DataFetcherResult.Builder<T> fetcherResultBuilder = DataFetcherResult.newResult();
		fetcherResultBuilder = fetcherResultBuilder.data(result.data);
		for(GraphQlErrorDto error : result.errors){
			fetcherResultBuilder.error(new DatarouterGraphQlDataValidationError(error, environment.getDocument()
					.getSourceLocation(), environment.getExecutionStepInfo().getPath()));
		}
		return fetcherResultBuilder.build();
	}

}
