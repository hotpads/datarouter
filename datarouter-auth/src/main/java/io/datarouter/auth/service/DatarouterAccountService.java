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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.auth.cache.DatarouterAccountPermissionKeysByPrefixCache;
import io.datarouter.auth.storage.account.BaseDatarouterAccountCredentialDao;
import io.datarouter.auth.storage.account.BaseDatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountCredential;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionKey;
import io.datarouter.auth.storage.useraccountmap.BaseDatarouterUserAccountMapDao;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMapKey;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.session.service.Session;
import io.datarouter.web.user.session.service.SessionBasedUser;
import io.datarouter.web.util.http.RequestTool;

@Singleton
public class DatarouterAccountService{

	private final BaseDatarouterAccountDao datarouterAccountDao;
	private final BaseDatarouterAccountCredentialDao datarouterAccountCredentialDao;
	private final BaseDatarouterUserAccountMapDao datarouterUserAccountMapDao;
	private final DatarouterAccountPermissionKeysByPrefixCache datarouterAccountPermissionKeysByPrefixCache;
	private final DatarouterAccountLastUsedDateService datarouterAccountLastUsedDateService;

	@Inject
	public DatarouterAccountService(
			BaseDatarouterAccountDao datarouterAccountDao,
			BaseDatarouterAccountCredentialDao datarouterAccountCredentialDao,
			BaseDatarouterUserAccountMapDao datarouterUserAccountMapDao,
			DatarouterAccountPermissionKeysByPrefixCache datarouterAccountPermissionKeysByPrefixCache,
			DatarouterAccountLastUsedDateService datarouterAccountLastUsedDateService){
		this.datarouterAccountDao = datarouterAccountDao;
		this.datarouterAccountCredentialDao = datarouterAccountCredentialDao;
		this.datarouterUserAccountMapDao = datarouterUserAccountMapDao;
		this.datarouterAccountPermissionKeysByPrefixCache = datarouterAccountPermissionKeysByPrefixCache;
		this.datarouterAccountLastUsedDateService = datarouterAccountLastUsedDateService;
	}

	public List<String> getAllAccountNamesWithUserMappingsEnabled(){
		return datarouterAccountDao.scan()
				.include(DatarouterAccount::getEnableUserMappings)
				.map(DatarouterAccount::getKey)
				.map(DatarouterAccountKey::getAccountName)
				.list();
	}

	//API key/request inputs

	//intended for API key auth (updates last used date of key)
	public Scanner<DatarouterAccountPermissionKey> scanPermissionsForApiKeyAuth(String apiKey){
		return findAccountCredentialForApiKeyAuth(apiKey)
				.map(DatarouterAccountCredential::getAccountName)
				.map(DatarouterAccountPermissionKey::new)
				.map(datarouterAccountPermissionKeysByPrefixCache::get)
				.map(Scanner::of)
				.orElseGet(Scanner::empty);
	}

	//intended for API key auth (updates last used date of key)
	public Optional<DatarouterAccountCredential> findAccountCredentialForApiKeyAuth(String apiKey){
		Optional<DatarouterAccountCredential> accountCredential = datarouterAccountCredentialDao
				.getFromAccountCredentialByApiKeyCache(apiKey);
		accountCredential.ifPresent(datarouterAccountLastUsedDateService::updateLastUsedDate);
		return accountCredential;
	}


	public Optional<String> getCurrentDatarouterAccountName(HttpServletRequest request){
		String apiKey = RequestTool.getParameterOrHeader(request, SecurityParameters.API_KEY);
		return Optional.ofNullable(apiKey)
				.flatMap(datarouterAccountCredentialDao::getFromAccountCredentialByApiKeyCache)
				.map(DatarouterAccountCredential::getAccountName);
	}

	public String getAccountNameForRequest(HttpServletRequest request){
		return getCurrentDatarouterAccountName(request)
				.orElseThrow();
	}

	//Session/SessionBasedUser inputs

	public boolean userCanAccessAccount(Session session, String accountName){
		if(!userCanAccessAccount(session.getUserId(), accountName)){
			return false;
		}
		return datarouterAccountDao.find(new DatarouterAccountKey(accountName))
				.map(DatarouterAccount::getEnableUserMappings)
				.orElse(false);

	}

	private boolean userCanAccessAccount(Long userId, String accountName){
		if(userId == null){
			return false;
		}
		DatarouterUserAccountMapKey key = new DatarouterUserAccountMapKey(userId, accountName);
		return datarouterUserAccountMapDao.exists(key);
	}

	public Set<String> findAccountNamesForUser(SessionBasedUser user){
		return scanAccountNamesForUserIdWithUserMappingEnabled(user.getId())
				.collect(HashSet::new);
	}

	public Set<String> findAccountNamesForUser(Session session){
		return scanAccountNamesForUserIdWithUserMappingEnabled(session.getUserId())
				.collect(HashSet::new);
	}

	public Optional<DatarouterAccountCredential> findFirstAccountCredentialForUser(Session session){
		if(session.getUserId() == null){
			return Optional.empty();
		}
		return scanAccountNamesForUserIdWithUserMappingEnabled(session.getUserId())
				.map(datarouterAccountCredentialDao::getFromAccountCredentialByAccountNameCache)
				.include(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}

	public Scanner<DatarouterAccount> scanAccountsForUser(Session session){
		return scanAccountsForUserIdWithUserMappingEnabled(session.getUserId());
	}

	private Scanner<String> scanAccountNamesForUserIdWithUserMappingEnabled(Long userId){
		if(userId == null){
			return Scanner.empty();
		}
		return scanAccountsForUserIdWithUserMappingEnabled(userId)
				.map(DatarouterAccount::getKey)
				.map(DatarouterAccountKey::getAccountName);
	}

	private Scanner<DatarouterAccount> scanAccountsForUserIdWithUserMappingEnabled(Long userId){
		if(userId == null){
			return Scanner.empty();
		}
		var prefix = new DatarouterUserAccountMapKey(userId, null);
		return datarouterUserAccountMapDao.scanKeysWithPrefix(prefix)
				.map(DatarouterUserAccountMapKey::getDatarouterAccountKey)
				.listTo(datarouterAccountDao::scanMulti)
				.include(DatarouterAccount::getEnableUserMappings);
	}

}
