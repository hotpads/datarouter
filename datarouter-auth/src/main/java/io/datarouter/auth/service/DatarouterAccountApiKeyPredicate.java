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
package io.datarouter.auth.service;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionKey;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.dispatcher.ApiKeyPredicate;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.util.http.RequestTool;

@Singleton
public class DatarouterAccountApiKeyPredicate implements ApiKeyPredicate{

	private final DatarouterAccountService datarouterAccountService;
	private final DatarouterAccountCounters datarouterAccountCounters;

	@Inject
	public DatarouterAccountApiKeyPredicate(
			DatarouterAccountService datarouterAccountService,
			DatarouterAccountCounters datarouterAccountCounters){
		this.datarouterAccountService = datarouterAccountService;
		this.datarouterAccountCounters = datarouterAccountCounters;
	}

	@Override
	public Pair<Boolean,String> check(DispatchRule rule, HttpServletRequest request){
		String apiKeyCandidate = RequestTool.getParameterOrHeader(request, SecurityParameters.API_KEY);
		if(apiKeyCandidate == null){
			return new Pair<>(false, "key not found");
		}
		Optional<String> endpoint = rule.getPersistentString();
		return new Pair<>(check(
				endpoint,
				apiKeyCandidate).isPresent(),
				"no account for " + ApiKeyPredicate.obfuscate(apiKeyCandidate));
	}

	public Optional<String> check(Optional<String> endpoint, String apiKeyCandidate){
		Optional<DatarouterAccountPermissionKey> permission = datarouterAccountService.scanPermissionsForApiKey(
				apiKeyCandidate)
				.include(candidate -> isValidEndpoint(candidate, endpoint))
				.findAny();
		permission.ifPresent(datarouterAccountCounters::incPermissionUsage);
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
