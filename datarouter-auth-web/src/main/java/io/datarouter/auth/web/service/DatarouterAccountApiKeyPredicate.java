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

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.auth.storage.account.permission.DatarouterAccountPermissionKey;
import io.datarouter.auth.web.config.metrics.DatarouterAccountMetrics;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.web.dispatcher.ApiKeyPredicate;
import io.datarouter.web.dispatcher.DispatchRule;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAccountApiKeyPredicate extends ApiKeyPredicate{

	private final DatarouterAccountCredentialService datarouterAccountCredentialService;
	private final DatarouterAccountMetrics datarouterAccountMetrics;

	@Singleton
	public static class DatarouterAccountApiKeyPredicateFactory{

		@Inject
		private DatarouterAccountCredentialService datarouterAccountCredentialService;
		@Inject
		private DatarouterAccountMetrics datarouterAccountMetrics;

		public DatarouterAccountApiKeyPredicate create(String apiKeyFieldName){
			return new DatarouterAccountApiKeyPredicate(
					apiKeyFieldName,
					datarouterAccountCredentialService,
					datarouterAccountMetrics);
		}

	}

	@Inject
	public DatarouterAccountApiKeyPredicate(
			DatarouterAccountCredentialService datarouterAccountApiKeyService,
			DatarouterAccountMetrics datarouterAccountCounters){
		this(SecurityParameters.API_KEY, datarouterAccountApiKeyService, datarouterAccountCounters);
	}

	private DatarouterAccountApiKeyPredicate(
			String apiKeyFieldName,
			DatarouterAccountCredentialService datarouterAccountCredentialService,
			DatarouterAccountMetrics datarouterAccountMetrics){
		super(apiKeyFieldName);
		this.datarouterAccountCredentialService = datarouterAccountCredentialService;
		this.datarouterAccountMetrics = datarouterAccountMetrics;
	}

	@Override
	public ApiKeyPredicateCheck innerCheck(DispatchRule rule, HttpServletRequest request, String apiKeyCandidate){
		Optional<String> endpoint = rule.getPersistentString();
		return check(endpoint, apiKeyCandidate)
				.map(accountName -> new ApiKeyPredicateCheck(true, accountName))
				.orElseGet(() -> new ApiKeyPredicateCheck(false, "no account for " + obfuscate(apiKeyCandidate)));
	}

	public Optional<String> check(Optional<String> endpoint, String apiKeyCandidate){
		Optional<DatarouterAccountPermissionKey> permission = datarouterAccountCredentialService
				.scanPermissionsForApiKeyAuth(apiKeyCandidate)
				.include(candidate -> isValidEndpoint(candidate, endpoint))
				.findFirst();
		permission.ifPresent(datarouterAccountMetrics::incPermissionUsage);
		return permission
				.map(DatarouterAccountPermissionKey::getAccountName);
	}

	private boolean isValidEndpoint(DatarouterAccountPermissionKey candidate, Optional<String> endpoint){
		boolean isWildcard = candidate.getEndpoint().equals(DatarouterAccountPermissionKey.ALL_ENDPOINTS);
		boolean matches = endpoint
				.map(candidate.getEndpoint()::equals)
				.orElse(false);
		return isWildcard || matches;
	}

}
