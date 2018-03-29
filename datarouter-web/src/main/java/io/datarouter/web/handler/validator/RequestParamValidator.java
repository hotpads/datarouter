/**
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

public abstract class RequestParamValidator<T>{

	protected String parameterName;

	public static class RequestParamValidatorResponseDto{
		public final boolean success;
		public final String errorMessage;

		private RequestParamValidatorResponseDto(boolean success, String errorMessage){
			this.success = success;
			this.errorMessage = errorMessage;
		}

		public static RequestParamValidatorResponseDto makeErrorResponse(String errorMessage){
			return new RequestParamValidatorResponseDto(false, errorMessage);
		}

		public static RequestParamValidatorResponseDto makeSuccessResponse(){
			return new RequestParamValidatorResponseDto(true, null);
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
