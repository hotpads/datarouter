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
import java.util.Optional;
import java.util.Set;

import io.datarouter.auth.session.Session;
import io.datarouter.auth.session.SessionBasedUser;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredential;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredentialDao;
import io.datarouter.auth.storage.user.useraccountmap.DatarouterUserAccountMapDao;
import io.datarouter.auth.storage.user.useraccountmap.DatarouterUserAccountMapKey;
import io.datarouter.scanner.Scanner;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAccountUserService{

	@Inject
	private DatarouterAccountDao datarouterAccountDao;
	@Inject
	private DatarouterAccountCredentialDao datarouterAccountCredentialDao;
	@Inject
	private DatarouterUserAccountMapDao datarouterUserAccountMapDao;

	public List<String> getAllAccountNamesWithUserMappingsEnabled(){
		return datarouterAccountDao.scan()
				.include(DatarouterAccount::getEnableUserMappings)
				.map(DatarouterAccount::getKey)
				.map(DatarouterAccountKey::getAccountName)
				.list();
	}

	public Set<String> findAccountNamesForUser(SessionBasedUser user){
		return scanAccountNamesForUserIdWithUserMappingEnabled(user.getId())
				.collect(HashSet::new);
	}

	public Set<String> findAccountNamesForUser(Session session){
		return scanAccountNamesForUserIdWithUserMappingEnabled(session.getUserId())
				.collect(HashSet::new);
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
