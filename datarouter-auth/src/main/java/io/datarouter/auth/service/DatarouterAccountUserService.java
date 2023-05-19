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
package io.datarouter.auth.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.account.BaseDatarouterAccountCredentialDao;
import io.datarouter.auth.storage.account.BaseDatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountCredential;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.useraccountmap.BaseDatarouterUserAccountMapDao;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMapKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.session.service.Session;
import io.datarouter.web.user.session.service.SessionBasedUser;

@Singleton
public class DatarouterAccountUserService{

	private final BaseDatarouterAccountDao datarouterAccountDao;
	private final BaseDatarouterAccountCredentialDao datarouterAccountCredentialDao;
	private final BaseDatarouterUserAccountMapDao datarouterUserAccountMapDao;

	@Inject
	public DatarouterAccountUserService(
			BaseDatarouterAccountDao datarouterAccountDao,
			BaseDatarouterAccountCredentialDao datarouterAccountCredentialDao,
			BaseDatarouterUserAccountMapDao datarouterUserAccountMapDao){
		this.datarouterAccountDao = datarouterAccountDao;
		this.datarouterAccountCredentialDao = datarouterAccountCredentialDao;
		this.datarouterUserAccountMapDao = datarouterUserAccountMapDao;
	}

	public List<String> getAllAccountNamesWithUserMappingsEnabled(){
		return datarouterAccountDao.scan()
				.include(DatarouterAccount::getEnableUserMappings)
				.map(DatarouterAccount::getKey)
				.map(DatarouterAccountKey::getAccountName)
				.list();
	}

	public Scanner<String> scanAllAccountNames(){
		return datarouterAccountDao.scan()
				.map(DatarouterAccount::getKey)
				.map(DatarouterAccountKey::getAccountName);
	}

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

	public Map<String,Boolean> getAccountProvisioningStatusForUser(DatarouterUser user){
		List<String> availableAccounts = getAllAccountNamesWithUserMappingsEnabled();
		Set<String> currentAccounts = findAccountNamesForUser(user);
		return Scanner.of(availableAccounts)
				.toMap(Function.identity(), currentAccounts::contains);
	}

	//note: this does not return credentials from DatarouterAccountSecretCredentials
	public Optional<DatarouterAccountCredential> findFirstAccountCredentialForUser(Session session){
		Set<String> accountsForUser = findAccountNamesForUser(session);
		if(accountsForUser.isEmpty()){
			return Optional.empty();
		}
		return datarouterAccountCredentialDao.scanByAccountNames(accountsForUser)
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
