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
package io.datarouter.graphql.error;

import java.util.List;
import java.util.Optional;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;
import io.datarouter.graphql.client.util.response.GraphQlErrorDto;

@SuppressWarnings("serial")
public class DatarouterGraphQlDataValidationError implements GraphQLError{

	private final GraphQlErrorDto error;
	private final String message;
	private final List<SourceLocation> locations;
	private final List<Object> path;

	public DatarouterGraphQlDataValidationError(
			GraphQlErrorDto error,
			SourceLocation sourceLocation,
			ResultPath executionPath){
		this.message = error.message;
		this.error = GraphQlErrorDto.with(error.code, error.message, Optional.of(executionPath.toString()));
		this.locations = List.of(sourceLocation);
		this.path = executionPath.toList();
	}

	public GraphQlErrorDto getError(){
		return this.error;
	}

	@Override
	public List<Object> getPath(){
		return this.path;
	}

	@Override
	public String getMessage(){
		return this.message;
	}

	@Override
	public List<SourceLocation> getLocations(){
		return this.locations;
	}

	@Override
	public ErrorType getErrorType(){
		return ErrorType.ValidationError;
	}

	@Override
	public String toString(){
		return String.format("GraphQlDataValidationError{code=%s, message=\"%s\"}", error.code, message);
	}

}
