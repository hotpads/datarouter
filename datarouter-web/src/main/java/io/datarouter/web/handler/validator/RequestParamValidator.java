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
package io.datarouter.web.handler.validator;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpStatus;

public abstract class RequestParamValidator<T>{

	protected String parameterName;

	public static class RequestParamValidatorResponseDto{

		public final boolean success;
		public final int statusCode;
		public final String errorMessage;

		private RequestParamValidatorResponseDto(boolean success, int statusCode, String errorMessage){
			this.success = success;
			this.statusCode = statusCode;
			this.errorMessage = errorMessage;
		}

		public static RequestParamValidatorResponseDto makeErrorResponse(String errorMessage){
			return makeErrorResponse(errorMessage, HttpStatus.SC_BAD_REQUEST);
		}

		private static RequestParamValidatorResponseDto makeErrorResponse(String errorMessage, int statusCode){
			return new RequestParamValidatorResponseDto(false, statusCode, errorMessage);
		}

		public static RequestParamValidatorResponseDto makeUnavailableErrorResponse(String errorMessage){
			return makeErrorResponse(errorMessage, HttpStatus.SC_SERVICE_UNAVAILABLE);
		}

		public static RequestParamValidatorResponseDto makeForbiddenErrorResponse(String errorMessage){
			return makeErrorResponse(errorMessage, HttpStatus.SC_FORBIDDEN);
		}

		public static RequestParamValidatorResponseDto makeSuccessResponse(){
			return makeSuccessResponse(HttpStatus.SC_OK);
		}

		private static RequestParamValidatorResponseDto makeSuccessResponse(int statusCode){
			return new RequestParamValidatorResponseDto(true, statusCode, null);
		}
	}

	public static class RequestParamValidatorErrorResponseDto{

		public final String message;
		public final int statusCode;

		public RequestParamValidatorErrorResponseDto(String message, int statusCode){
			this.message = message;
			this.statusCode = statusCode;
		}

		public static RequestParamValidatorErrorResponseDto fromRequestParamValidatorResponseDto(
				RequestParamValidatorResponseDto responseDto){
			return new RequestParamValidatorErrorResponseDto(responseDto.errorMessage, responseDto.statusCode);
		}
	}

	public void setParameterName(String parameterName){
		this.parameterName = parameterName;
	}

	public abstract RequestParamValidatorResponseDto validate(HttpServletRequest request, T parameterValue);
	public abstract Class<T> getParameterClass();

	public String getParameterName(){
		return parameterName;
	}

}
