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

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredential;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredentialDao;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredentialKey;
import io.datarouter.auth.storage.account.credential.secret.DatarouterAccountSecretCredentialDao;
import io.datarouter.auth.storage.account.credential.secret.DatarouterAccountSecretCredentialKey;
import io.datarouter.types.MilliTime;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAccountLastUsedDateService{

	private final DatarouterAccountDao accountDao;
	private final DatarouterAccountCredentialDao credentialDao;
	private final DatarouterAccountSecretCredentialDao secretCredentialDao;
	private final Map<DatarouterAccountKey,MilliTime> lastUsedByAccount;
	private final Map<DatarouterAccountCredentialKey,MilliTime> lastUsedByCredential;
	private final Map<DatarouterAccountSecretCredentialKey,MilliTime> lastUsedBySecretCredential;

	@Inject
	public DatarouterAccountLastUsedDateService(
			DatarouterAccountDao accountDao,
			DatarouterAccountCredentialDao credentialDao,
			DatarouterAccountSecretCredentialDao secretCredentialDao){
		this.accountDao = accountDao;
		this.credentialDao = credentialDao;
		this.secretCredentialDao = secretCredentialDao;
		this.lastUsedByAccount = new ConcurrentHashMap<>();
		this.lastUsedByCredential = new ConcurrentHashMap<>();
		this.lastUsedBySecretCredential = new ConcurrentHashMap<>();
	}

	public void updateLastUsedDateForCredential(DatarouterAccountCredential credential){
		lastUsedByCredential.put(credential.getKey(), MilliTime.now());
		lastUsedByAccount.put(new DatarouterAccountKey(credential.getAccountName()), MilliTime.now());
	}

	public void updateLastUsedDateForCredential(DatarouterAccountCredentialKey key, String accountName){
		lastUsedByCredential.put(key, MilliTime.now());
		lastUsedByAccount.put(new DatarouterAccountKey(accountName), MilliTime.now());
	}

	public void updateLastUsedDateForSecretCredential(DatarouterAccountSecretCredentialKey key, String accountName){
		lastUsedBySecretCredential.put(key, MilliTime.now());
		lastUsedByAccount.put(new DatarouterAccountKey(accountName), MilliTime.now());
	}

	public void flush(){
		//make a copy of keySet() instead of using it directly (because keySet() is backed by the Map)
		var accountKeys = new HashSet<>(lastUsedByAccount.keySet());
		accountDao.scanMulti(accountKeys)
				.include(account -> account.getLastUsed() == null
						|| lastUsedByAccount.get(account.getKey()).isAfter(account.getLastUsed()))
				.each(account -> account.setLastUsed(lastUsedByAccount.get(account.getKey())))
				.flush(accountDao::putMulti);
		//remove all the copied keys from the Map, even if they weren't found/updated in the DB
		accountKeys.forEach(lastUsedByAccount::remove);

		var credentialKeys = new HashSet<>(lastUsedByCredential.keySet());
		credentialDao.scanMulti(credentialKeys)
				.include(credential -> credential.getLastUsed() == null
						|| lastUsedByCredential.get(credential.getKey()).isAfter(credential.getLastUsed()))
				.each(credential -> credential.setLastUsed(lastUsedByCredential.get(credential.getKey())))
				.flush(credentialDao::updateMultiIgnore);
		credentialKeys.forEach(lastUsedByCredential::remove);

		var secretCredentialKeys = new HashSet<>(lastUsedBySecretCredential.keySet());
		secretCredentialDao.scanMulti(secretCredentialKeys)
				.include(secretCredential -> secretCredential.getLastUsed() == null
						|| lastUsedBySecretCredential.get(secretCredential.getKey())
								.isAfter(secretCredential.getLastUsed()))
				.each(credential -> credential.setLastUsed(lastUsedBySecretCredential.get(credential.getKey())))
				.flush(secretCredentialDao::updateMultiIgnore);
		secretCredentialKeys.forEach(lastUsedBySecretCredential::remove);
	}

}
