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

import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.httpclient.security.SecurityParameters;

public class DefaultApiKeyPredicate extends ApiKeyPredicate{

	private final Supplier<String> apiKeySupplier;

	public DefaultApiKeyPredicate(Supplier<String> apiKeySupplier){
		super(SecurityParameters.API_KEY);
		this.apiKeySupplier = apiKeySupplier;
	}

	@Override
	public ApiKeyPredicateCheck innerCheck(DispatchRule rule, HttpServletRequest request, String apiKeyCandidate){
		if(apiKeySupplier.get().equals(apiKeyCandidate)){
			return new ApiKeyPredicateCheck(true, "");
		}
		return new ApiKeyPredicateCheck(false, "no match for " + obfuscate(apiKeyCandidate));
	}

}
