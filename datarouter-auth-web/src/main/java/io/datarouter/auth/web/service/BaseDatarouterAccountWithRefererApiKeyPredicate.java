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
package io.datarouter.auth.web.service;

import java.util.function.Predicate;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.auth.web.config.metrics.DatarouterAccountMetrics;
import io.datarouter.auth.web.service.DatarouterAccountRefererService.DatarouterAccountRefererCheck;
import io.datarouter.web.dispatcher.DispatchRule;

public abstract class BaseDatarouterAccountWithRefererApiKeyPredicate extends DatarouterAccountApiKeyPredicate{

	private final DatarouterAccountRefererService accountRefererService;
	private final Predicate<HttpServletRequest> rateLimiter;

	public BaseDatarouterAccountWithRefererApiKeyPredicate(
			DatarouterAccountCredentialService datarouterAccountApiKeyService,
			DatarouterAccountMetrics datarouterAccountMetrics,
			DatarouterAccountRefererService accountRefererService,
			Predicate<HttpServletRequest> rateLimiter){
		super(datarouterAccountApiKeyService, datarouterAccountMetrics);
		this.accountRefererService = accountRefererService;
		this.rateLimiter = rateLimiter;
	}

	@Override
	public ApiKeyPredicateCheck innerCheck(DispatchRule rule, HttpServletRequest request, String apiKeyCandidate){
		ApiKeyPredicateCheck check = super.innerCheck(rule, request, apiKeyCandidate);

		if(check.allowed()){
			DatarouterAccountRefererCheck refererCheck = accountRefererService.validateAccountReferer(
					check.accountName(), request);

			if(!refererCheck.allowed()){
				return new ApiKeyPredicateCheck(false, "invalid referer for " + obfuscate(apiKeyCandidate));
			}

			//only rate limit if account has referer validation
			if(refererCheck.hasRefererValidation() && !rateLimiter.test(request)){
				return new ApiKeyPredicateCheck(false, "rate limit exceeded");
			}
		}

		return check;
	}

}
