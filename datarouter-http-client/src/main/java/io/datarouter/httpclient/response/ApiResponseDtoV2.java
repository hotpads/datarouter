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
import java.util.Optional;
import java.util.function.Function;

import org.apache.http.HttpStatus;

import io.datarouter.httpclient.DocumentedGenericHolder;

public record ApiResponseDtoV2<T>(
		T response,
		boolean success,
		ApiResponseErrorDtoV2 error,
		int httpStatus)
implements DocumentedGenericHolder{

	@Override
	public List<String> getGenericFieldNames(){
		return List.of("response");
	}

	public static <T> ApiResponseDtoV2<T> successResponse(T response){
		return successResponse(response, HttpStatus.SC_OK);
	}

	public static <T> ApiResponseDtoV2<T> successResponse(T response, int httpsStatus){
		return new ApiResponseDtoV2<>(response, true, null, httpsStatus);
	}

	public static <T> ApiResponseDtoV2<T> createdSuccess(T response){
		return successResponse(response, HttpStatus.SC_CREATED);
	}

	public static <T> ApiResponseDtoV2<T> internalError(String message){
		return errorResponse(message, null, HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	public static <T> ApiResponseDtoV2<T> unavailableError(String message){
		return errorResponse(message, null, HttpStatus.SC_SERVICE_UNAVAILABLE);
	}

	public static <T> ApiResponseDtoV2<T> badRequestError(String message){
		return errorResponse(message, null, HttpStatus.SC_BAD_REQUEST);
	}

	public static <T> ApiResponseDtoV2<T> notFoundError(String message){
		return errorResponse(message, null, HttpStatus.SC_NOT_FOUND);
	}

	public static <T> ApiResponseDtoV2<T> forbidden(String message){
		return errorResponse(message, null, HttpStatus.SC_FORBIDDEN);
	}

	public static <T> ApiResponseDtoV2<T> errorResponse(
			String message,
			String code,
			int httpStatus){
		ApiResponseErrorDtoV2 error = new ApiResponseErrorDtoV2(message, code);
		return new ApiResponseDtoV2<>(null, false, error, httpStatus);
	}

	public <U> ApiResponseDtoV2<U> map(
			Function<? super T,? extends U> successMapper){
		if(success){
			return ApiResponseDtoV2.successResponse(successMapper.apply(response));
		}
		return new ApiResponseDtoV2<>(null, false, error, httpStatus);
	}

	public Optional<T> getResponse(){
		return Optional.ofNullable(response);
	}

	public Optional<ApiResponseErrorDtoV2> getError(){
		return Optional.ofNullable(error);
	}

}
