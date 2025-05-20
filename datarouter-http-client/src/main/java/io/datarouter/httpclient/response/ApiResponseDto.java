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

public class ApiResponseDto<T> implements DocumentedGenericHolder{

	private final T response;
	private final boolean success;
	private final ApiResponseErrorDto<T> error;
	private final int httpStatus;

	public ApiResponseDto(
			T response,
			boolean success,
			ApiResponseErrorDto<T> error,
			int httpStatus){
		this.response = response;
		this.success = success;
		this.error = error;
		this.httpStatus = httpStatus;
	}

	@Override
	public final List<String> getGenericFieldNames(){
		return List.of("response");
	}

	/**
	 * @deprecated inline
	 */
	@Deprecated
	public final T response(){
		return getResponse();
	}

	/**
	 * @deprecated inline
	 */
	@Deprecated
	public final int httpStatus(){
		return getHttpStatus();
	}

	/**
	 * @deprecated inline
	 */
	@Deprecated
	public final boolean success(){
		return getSuccess();
	}

	public static <T> ApiResponseDto<T> success(T response){
		return success(response, HttpStatus.SC_OK);
	}

	public static <T> ApiResponseDto<T> success(T response, int httpServletResponseCode){
		return new ApiResponseDto<>(response, true, null, httpServletResponseCode);
	}

	public static <T> ApiResponseDto<T> createdSuccess(T response){
		return success(response, HttpStatus.SC_CREATED);
	}

	public static <T> ApiResponseDto<T> internalError(String message){
		return error(message, null, HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	public static <T> ApiResponseDto<T> unavailableError(String message){
		return error(message, null, HttpStatus.SC_SERVICE_UNAVAILABLE);
	}

	public static <T> ApiResponseDto<T> badRequestError(String message){
		return error(message, null, HttpStatus.SC_BAD_REQUEST);
	}

	public static <T> ApiResponseDto<T> notFoundError(String message){
		return error(message, null, HttpStatus.SC_NOT_FOUND);
	}

	public static <T> ApiResponseDto<T> methodNotAllowedError(String message){
		return error(message, null, HttpStatus.SC_METHOD_NOT_ALLOWED);
	}

	public static <T> ApiResponseDto<T> forbidden(String message){
		return error(message, null, HttpStatus.SC_FORBIDDEN);
	}

	public static <T> ApiResponseDto<T> error(String message){
		return error(message, null, HttpStatus.SC_FORBIDDEN);
	}

	public static <T> ApiResponseDto<T> error(
			String message,
			String code,
			T response,
			int httpStatus){
		ApiResponseErrorDto<T> error = new ApiResponseErrorDto<>(message, code, response);
		return new ApiResponseDto<>(null, false, error, httpStatus);
	}

	public static <T> ApiResponseDto<T> error(String message, T response, int httpStatus){
		return new ApiResponseDto<>(
				null,
				false,
				new ApiResponseErrorDto<>(message, null, response),
				httpStatus);
	}

	/**
	 * @deprecated inline
	 */
	@Deprecated
	public final ApiResponseErrorDto<T> error(){
		return getError();
	}

	public final T getResponse(){
		return response;
	}

	public final boolean getSuccess(){
		return success;
	}

	public final ApiResponseErrorDto<T> getError(){
		return error;
	}

	public final int getHttpStatus(){
		return httpStatus;
	}

	public <U> ApiResponseDto<U> map(Function<? super T,? extends U> mapper){
		if(success){
			return new ApiResponseDto<>(
					nullSafeApply(mapper, response),
					true,
					null,
					httpStatus);
		}
		return new ApiResponseDto<>(
				null,
				false,
				new ApiResponseErrorDto<>(error.message, error.code, nullSafeApply(mapper, error.data)),
				httpStatus);
	}

	private <U> U nullSafeApply(Function<? super T,? extends U> mapper, T data){
		return Optional.ofNullable(data)
				.map(mapper)
				.orElse(null);
	}

}
