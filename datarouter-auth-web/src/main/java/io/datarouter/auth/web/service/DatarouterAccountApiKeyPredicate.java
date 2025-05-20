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

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.auth.storage.account.permission.DatarouterAccountPermissionKey;
import io.datarouter.auth.web.config.metrics.DatarouterAccountMetrics;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.scanner.Scanner;
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
		List<DatarouterAccountPermissionKey> matchingAccountPermissions = datarouterAccountCredentialService
				.scanPermissionsForApiKeyAuth(apiKeyCandidate)
				.list();
		if(matchingAccountPermissions.isEmpty()){
			Optional<String> accountName = datarouterAccountCredentialService.findAccountNameForApiKey(apiKeyCandidate);
			return accountName.map(account ->
							new ApiKeyPredicateCheck(false, null, "no permissions found for account=" + account))
					.orElseGet(() -> new ApiKeyPredicateCheck(false, null,
							"no account found for apiKey=" + obfuscate(apiKeyCandidate)));
		}
		Optional<DatarouterAccountPermissionKey> matchingAccountWithAccess = Scanner.of(matchingAccountPermissions)
				.include(candidate -> isValidEndpoint(candidate, endpoint))
				.findFirst();
		if(matchingAccountWithAccess.isEmpty()){
			String errorMessage = "matching account=" + matchingAccountPermissions.getFirst().getAccountName()
					+ " does not have access to " + rule.getPersistentString().orElse("this endpoint");
			return new ApiKeyPredicateCheck(false, null, errorMessage);
		}
		datarouterAccountMetrics.incPermissionUsage(matchingAccountWithAccess.get());
		return new ApiKeyPredicateCheck(true, matchingAccountWithAccess.get().getAccountName(), null);
	}

	private boolean isValidEndpoint(DatarouterAccountPermissionKey candidate, Optional<String> endpoint){
		boolean isWildcard = candidate.getEndpoint().equals(DatarouterAccountPermissionKey.ALL_ENDPOINTS);
		boolean matches = endpoint
				.map(candidate.getEndpoint()::equals)
				.orElse(false);
		return isWildcard || matches;
	}

}
