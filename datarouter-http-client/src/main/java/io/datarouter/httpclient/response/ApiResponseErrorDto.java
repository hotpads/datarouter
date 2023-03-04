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
package io.datarouter.httpclient.response;

import java.util.List;

import io.datarouter.httpclient.DocumentedGenericHolder;

public class ApiResponseErrorDto<T> implements DocumentedGenericHolder{

	public final String message;
	public final String code;
	public final T data;

	public ApiResponseErrorDto(String message, String code, T data){
		this.message = message;
		this.code = code;
		this.data = data;
	}

	public ApiResponseErrorDto(String message, T data){
		this(message, null, data);
	}

	@Override
	public List<String> getGenericFieldNames(){
		return List.of("data");
	}

	public String message(){
		return message;
	}

	public String code(){
		return code;
	}

	public T data(){
		return data;
	}

}
