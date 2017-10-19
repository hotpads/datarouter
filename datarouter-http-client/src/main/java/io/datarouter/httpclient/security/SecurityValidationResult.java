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
package io.datarouter.httpclient.security;

import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

public class SecurityValidationResult{
	private final boolean success;
	// security validation may require reading the request body.
	// caller should use this wrapped request after validation.
	private final HttpServletRequest wrappedRequest;
	private String failureMessage;

	public static SecurityValidationResult success(HttpServletRequest request){
		return new SecurityValidationResult(request, true, null);
	}

	public static SecurityValidationResult failure(HttpServletRequest request){
		return new SecurityValidationResult(request, false, null);
	}

	public SecurityValidationResult(HttpServletRequest wrappedRequest, boolean checkPassed, String failureMessage){
		this.success = checkPassed;
		this.wrappedRequest = wrappedRequest;
		this.failureMessage = failureMessage;
	}

	public boolean isSuccess(){
		return success;
	}

	public HttpServletRequest getWrappedRequest(){
		return wrappedRequest;
	}

	public String getFailureMessage(){
		return failureMessage;
	}

	public SecurityValidationResult setFailureMessage(String message){
		this.failureMessage = message;
		return this;
	}

	public static SecurityValidationResult of(Function<HttpServletRequest,SecurityValidationResult> check,
			HttpServletRequest request){
		return check.apply(request);
	}

	public SecurityValidationResult combinedWith(Function<HttpServletRequest,SecurityValidationResult> nextCheck){
		if(this.success){
			return nextCheck.apply(this.wrappedRequest);
		}
		return this;
	}
}
