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
package io.datarouter.graphql.client.util.response;

import java.util.Optional;

public class GraphQlErrorDto{

	public final String code;
	public final String message;
	public final String path;

	public static GraphQlErrorDto with(String code, String message, Optional<String> path){
		return new GraphQlErrorDto(code, message, path.orElse(""));
	}

	public static GraphQlErrorDto invalidInput(String message){
		return invalidInput(message, Optional.empty());
	}

	public static GraphQlErrorDto invalidInput(String message, Optional<String> path){
		return with(DatarouterGraphQlErrorCode.INVALID_INPUT.name(), message, path);
	}

	public static GraphQlErrorDto internalError(String message){
		return internalError(message, Optional.empty());
	}

	public static GraphQlErrorDto internalError(String message, Optional<String> path){
		return with(DatarouterGraphQlErrorCode.INTERNAL_ERROR.name(), message, path);
	}

	public static GraphQlErrorDto illegalQuery(String message){
		return illegalQuery(message, Optional.empty());
	}

	public static GraphQlErrorDto illegalQuery(String message, Optional<String> path){
		return with(DatarouterGraphQlErrorCode.ILLEGAL_QUERY.name(), message, path);
	}

	private GraphQlErrorDto(String code, String message, String path){
		this.code = code;
		this.message = message;
		this.path = path;
	}

}
