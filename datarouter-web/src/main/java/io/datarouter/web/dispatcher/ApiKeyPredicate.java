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
package io.datarouter.web.dispatcher;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpHeaders;

import io.datarouter.web.util.http.RequestTool;

public abstract class ApiKeyPredicate{

	public static final String AUTHORIZATION_BEARER_PREFIX = "Bearer ";

	private final String apiKeyFieldName;

	public ApiKeyPredicate(String apiKeyFieldName){
		this.apiKeyFieldName = apiKeyFieldName;
	}

	// the string on the right is the account name or the error message
	public ApiKeyPredicateCheck check(DispatchRule rule, HttpServletRequest request){
		String apiKey = getApiKeyCandidate(request);
		if(apiKey == null){
			return new ApiKeyPredicateCheck(false, "key not found");
		}
		return innerCheck(rule, request, apiKey);
	}

	protected abstract ApiKeyPredicateCheck innerCheck(DispatchRule rule, HttpServletRequest request, String apiKey);

	public String getApiKeyFieldName(){
		return apiKeyFieldName;
	}

	public static String obfuscate(String apiKeyCandidate){
		int start = Math.min((apiKeyCandidate.length() - 1) / 2, 2);
		int end;
		if(apiKeyCandidate.length() > 5){
			end = apiKeyCandidate.length() - 2;
		}else{
			end = Math.max(apiKeyCandidate.length() - 1, 3);
		}
		int index = 0;
		var sb = new StringBuilder();
		for(; index < start; index++){
			sb.append(apiKeyCandidate.charAt(index));
		}
		for(; index < Math.min(end, apiKeyCandidate.length()); index++){
			sb.append('*');
		}
		for(; index < apiKeyCandidate.length(); index++){
			sb.append(apiKeyCandidate.charAt(index));
		}
		return sb.toString();
	}

	protected String getApiKeyCandidate(HttpServletRequest request){
		String apiKey = RequestTool.getParameterOrHeader(request, apiKeyFieldName);
		return apiKey == null ? getApiKeyFromBearerToken(request) : apiKey;
	}

	private static String getApiKeyFromBearerToken(HttpServletRequest request){
		return Optional.ofNullable(RequestTool.getParameterOrHeader(request, HttpHeaders.AUTHORIZATION))
				.filter(authHeader -> authHeader.startsWith(AUTHORIZATION_BEARER_PREFIX))
				.map(authHeader -> authHeader.substring(AUTHORIZATION_BEARER_PREFIX.length()))
				.orElse(null);
	}

	/**
	 *@param accountName the account name or the error message
	 */
	public record ApiKeyPredicateCheck(
			boolean allowed,
			String accountName){
	}

}
