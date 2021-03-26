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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.auth.cache.DatarouterAccountPermissionKeysByPrefixCache;
import io.datarouter.auth.storage.account.BaseDatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionKey;
import io.datarouter.auth.storage.useraccountmap.BaseDatarouterUserAccountMapDao;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMapKey;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.databean.DatarouterUserKey;
import io.datarouter.web.user.session.service.SessionBasedUser;
import io.datarouter.web.util.http.RequestTool;

@Singleton
public class DatarouterAccountService{

	private final BaseDatarouterAccountDao datarouterAccountDao;
	private final BaseDatarouterUserAccountMapDao datarouterUserAccountMapDao;
	private final DatarouterAccountPermissionKeysByPrefixCache datarouterAccountPermissionKeysByPrefixCache;
	private final DatarouterAccountLastUsedDateService datarouterAccountLastUsedDateService;

	@Inject
	public DatarouterAccountService(
			BaseDatarouterAccountDao datarouterAccountDao,
			BaseDatarouterUserAccountMapDao datarouterUserAccountMapDao,
			DatarouterAccountPermissionKeysByPrefixCache datarouterAccountPermissionKeysByPrefixCache,
			DatarouterAccountLastUsedDateService datarouterAccountLastUsedDateService){
		this.datarouterAccountDao = datarouterAccountDao;
		this.datarouterUserAccountMapDao = datarouterUserAccountMapDao;
		this.datarouterAccountPermissionKeysByPrefixCache = datarouterAccountPermissionKeysByPrefixCache;
		this.datarouterAccountLastUsedDateService = datarouterAccountLastUsedDateService;
	}

	public Optional<DatarouterAccount> getCurrentDatarouterAccount(HttpServletRequest request){
		String apiKey = RequestTool.getParameterOrHeader(request, SecurityParameters.API_KEY);
		return Optional.ofNullable(apiKey)
				.flatMap(this::findAccountForApiKey);
	}

	public Scanner<DatarouterAccountPermissionKey> scanPermissionsForApiKey(String apiKey){
		return findAccountForApiKey(apiKey)
				.map(DatarouterAccount::getKey)
				.map(DatarouterAccountKey::getAccountName)
				.map(DatarouterAccountPermissionKey::new)
				.map(datarouterAccountPermissionKeysByPrefixCache::get)
				.map(Scanner::of)
				.orElseGet(Scanner::empty);
	}

	public Optional<DatarouterAccount> findAccountForApiKey(String apiKey){
		Optional<DatarouterAccount> account = datarouterAccountDao.getFromAccountByApiKeyCache(apiKey);
		account.map(DatarouterAccount::getKey)
				.ifPresent(datarouterAccountLastUsedDateService::updateLastUsedDate);
		return account;
	}

	public boolean userCanAccessAccount(DatarouterUserKey userKey, String accountName){
		DatarouterUserAccountMapKey key = new DatarouterUserAccountMapKey(userKey.getId(), accountName);
		return datarouterUserAccountMapDao.exists(key);
	}

	public boolean userCanAccessAccountAndUserMappingsEnabled(DatarouterUserKey userKey, String accountName){
		DatarouterUserAccountMapKey key = new DatarouterUserAccountMapKey(userKey.getId(), accountName);
		boolean exists = datarouterUserAccountMapDao.exists(key);
		boolean userMappingEnabled = datarouterAccountDao.find(new DatarouterAccountKey(accountName))
				.map(DatarouterAccount::getEnableUserMappings)
				.orElse(false);
		return exists && userMappingEnabled;
	}

	public Set<String> findAccountNamesForUser(DatarouterUserKey userKey){
		return scanAccountKeysForUser(userKey.getId())
				.map(DatarouterAccountKey::getAccountName)
				.collect(HashSet::new);
	}

	public List<String> getAllAccountNamesWithUserMappingsEnabled(){
		return datarouterAccountDao.scan()
				.include(DatarouterAccount::getEnableUserMappings)
				.map(DatarouterAccount::getKey)
				.map(DatarouterAccountKey::getAccountName)
				.list();
	}

	public Set<String> findAccountNamesForUserWithUserMappingsEnabled(SessionBasedUser user){
		return scanAccountForUserWithUserMappingEnabled(user.getId())
				.map(DatarouterAccount::getKey)
				.map(DatarouterAccountKey::getAccountName)
				.collect(HashSet::new);
	}

	public Set<String> findAccountNamesForUserWithUserMappingsEnabled(DatarouterUserKey userKey){
		return scanAccountForUserWithUserMappingEnabled(userKey)
				.map(DatarouterAccount::getKey)
				.map(DatarouterAccountKey::getAccountName)
				.collect(HashSet::new);
	}

	public List<DatarouterAccount> findAccountsForUser(DatarouterUserKey userKey){
		return scanAccountKeysForUser(userKey.getId())
				.listTo(datarouterAccountDao::getMulti);
	}

	private Scanner<DatarouterAccountKey> scanAccountKeysForUser(Long id){
		DatarouterUserAccountMapKey prefix = new DatarouterUserAccountMapKey(id, null);
		return datarouterUserAccountMapDao.scanKeysWithPrefix(prefix)
				.map(DatarouterUserAccountMapKey::getDatarouterAccountKey);
	}

	public Scanner<DatarouterAccount> scanAccountForUserWithUserMappingEnabled(DatarouterUserKey userKey){
		return scanAccountForUserWithUserMappingEnabled(userKey.getId());
	}

	public Scanner<DatarouterAccount> scanAccountForUserWithUserMappingEnabled(Long userId){
		var prefix = new DatarouterUserAccountMapKey(userId, null);
		return datarouterUserAccountMapDao.scanKeysWithPrefix(prefix)
				.map(DatarouterUserAccountMapKey::getDatarouterAccountKey)
				.listTo(keys -> Scanner.of(datarouterAccountDao.getMulti(keys)))
				.include(DatarouterAccount::getEnableUserMappings);
	}

	public List<DatarouterAccount> getAccountsWithDuplicateApiKey(){
		Map<String,List<DatarouterAccount>> accountsByApiKey = datarouterAccountDao.scan()
				.groupBy(DatarouterAccount::getApiKey);
		return accountsByApiKey.values().stream()
				.filter(accounts -> accounts.size() > 1)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	public String getAccountNameForRequest(HttpServletRequest request){
		return getCurrentDatarouterAccount(request)
				.map(DatarouterAccount::getKey)
				.map(DatarouterAccountKey::getAccountName)
				.orElseThrow();
	}

}
