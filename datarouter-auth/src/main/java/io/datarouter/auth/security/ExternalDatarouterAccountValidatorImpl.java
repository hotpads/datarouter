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
package io.datarouter.auth.security;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.cached.CachedDatarouterAccounts;
import io.datarouter.auth.config.DatarouterAuthenticationSettings;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.httpclient.endpoint.caller.CallerTypeExternal;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ExternalDatarouterAccountValidatorImpl
implements ExternalDatarouterAccountValidator{
	private static final Logger logger = LoggerFactory.getLogger(ExternalDatarouterAccountValidatorImpl.class);

	@Inject
	private CachedDatarouterAccounts cachedDatarouterAccounts;
	@Inject
	private DatarouterAuthenticationSettings settings;

	@Override
	public boolean accountIsExternalCallerType(String accountName){
		CallerTypeExternal external = new CallerTypeExternal();
		DatarouterAccount account = cachedDatarouterAccounts.get()
				.get(accountName);
		if(account != null
				&& account.callerType != null
				&& !account.callerType.equals(external.getName())){
			logger.debug("callerType=[{}] must be external for this endpoint", account.callerType);
		}
		if(!settings.enforceExternalAccountCallerValidation.get()){
			return true;
		}
		return Optional.ofNullable(account)
				.map(DatarouterAccount::getCallerType)
				.map(type -> type.equals(external.getName()))
				.orElse(false);
	}

}
