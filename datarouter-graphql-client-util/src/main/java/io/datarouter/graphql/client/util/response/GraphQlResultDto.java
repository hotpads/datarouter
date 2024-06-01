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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphQlResultDto<T>{

	public final T data;
	public final List<GraphQlErrorDto> errors;
	public final Map<Object,Object> extensions;

	private GraphQlResultDto(T data, List<GraphQlErrorDto> errors, Map<Object,Object> extensions){
		this.data = data;
		this.errors = errors;
		this.extensions = extensions;
	}

	private GraphQlResultDto(T data, List<GraphQlErrorDto> errors){
		this(data, errors, null);
	}

	public boolean failed(){
		return data == null && errors != null && !errors.isEmpty();
	}

	public static <T> GraphQlResultDto<T> with(T data, List<GraphQlErrorDto> errors){
		return new GraphQlResultDto<>(data, errors);
	}

	public static <T> GraphQlResultDto<T> with(T data, GraphQlErrorDto error){
		return with(data, List.of(error));
	}

	public static <T> GraphQlResultDto<T> withData(T data){
		return with(data, new ArrayList<>());
	}

	public static <T> GraphQlResultDto<T> withErrors(List<GraphQlErrorDto> errors){
		return with(null, errors);
	}

	public static <T> GraphQlResultDto<T> withError(GraphQlErrorDto error){
		return withErrors(List.of(error));
	}

	public static <T> GraphQlResultDto<T> withGraphQlExtensions(
			T data,
			List<GraphQlErrorDto> errors,
			Map<Object,Object> extensions){
		return new GraphQlResultDto<>(data, errors, extensions);
	}

}
