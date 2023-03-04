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

import org.apache.http.HttpStatus;

import io.datarouter.httpclient.DocumentedGenericHolder;

public class ApiResponseDto<T> implements DocumentedGenericHolder{

	private final T response;
	private final boolean success;
	private final ApiResponseErrorDto<T> error;
	private final int httpStatus;

	public ApiResponseDto(T response, boolean success, ApiResponseErrorDto<T> error, int httpStatus){
		this.response = response;
		this.success = success;
		this.error = error;
		this.httpStatus = httpStatus;
	}

	@Override
	public final List<String> getGenericFieldNames(){
		return List.of("response");
	}

	public static <T> ApiResponseDto<T> makeSuccessResponse(T response){
		return makeSuccessResponse(response, HttpStatus.SC_OK);
	}

	public static <T> ApiResponseDto<T> makeSuccessResponse(T response, int httpServletResponseCode){
		return new ApiResponseDto<>(response, true, null, httpServletResponseCode);
	}

	public static <T> ApiResponseDto<T> makeCreatedSuccessResponse(T response){
		return makeSuccessResponse(response, HttpStatus.SC_CREATED);
	}

	public static <T> ApiResponseDto<T> makeInternalErrorResponse(String message){
		return makeErrorResponse(message, null, HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	public static <T> ApiResponseDto<T> makeUnavailableErrorResponse(String message){
		return makeErrorResponse(message, null, HttpStatus.SC_SERVICE_UNAVAILABLE);
	}

	public static <T> ApiResponseDto<T> makeBadRequestErrorResponse(String message){
		return makeErrorResponse(message, null, HttpStatus.SC_BAD_REQUEST);
	}

	public static <T> ApiResponseDto<T> makeNotFoundErrorResponse(String message){
		return makeErrorResponse(message, null, HttpStatus.SC_NOT_FOUND);
	}

	public static <T> ApiResponseDto<T> makeErrorResponse(String message){
		return makeErrorResponse(message, null, HttpStatus.SC_FORBIDDEN);
	}

	public static <T> ApiResponseDto<T> makeErrorResponse(
			String message,
			String code,
			T response,
			int httpStatus){
		ApiResponseErrorDto<T> error = new ApiResponseErrorDto<>(message, code, response);
		ApiResponseDto<T> dto = new ApiResponseDto<>(null, false, error, httpStatus);
		return dto;
	}

	public static <T> ApiResponseDto<T> makeErrorResponse(String message, T response, int httpStatus){
		return new ApiResponseDto<>(
				null,
				false,
				new ApiResponseErrorDto<>(message, null, response),
				httpStatus);
	}

	public final T response(){
		return response;
	}

	public final boolean success(){
		return success;
	}

	public final ApiResponseErrorDto<T> error(){
		return error;
	}

	public final int httpStatus(){
		return httpStatus;
	}

}
