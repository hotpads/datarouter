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

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.account.BaseDatarouterAccountCredentialDao;
import io.datarouter.auth.storage.account.BaseDatarouterAccountDao;
import io.datarouter.auth.storage.account.BaseDatarouterAccountSecretCredentialDao;
import io.datarouter.auth.storage.account.DatarouterAccountCredential;
import io.datarouter.auth.storage.account.DatarouterAccountCredentialKey;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.account.DatarouterAccountSecretCredentialKey;

@Singleton
public class DatarouterAccountLastUsedDateService{

	private final BaseDatarouterAccountDao accountDao;
	private final BaseDatarouterAccountCredentialDao credentialDao;
	private final BaseDatarouterAccountSecretCredentialDao secretCredentialDao;
	private final Map<DatarouterAccountKey,Date> lastUsedByAccount;
	private final Map<DatarouterAccountCredentialKey,Date> lastUsedByCredential;
	private final Map<DatarouterAccountSecretCredentialKey,Date> lastUsedBySecretCredential;

	@Inject
	public DatarouterAccountLastUsedDateService(BaseDatarouterAccountDao accountDao,
			BaseDatarouterAccountCredentialDao credentialDao,
			BaseDatarouterAccountSecretCredentialDao secretCredentialDao){
		this.accountDao = accountDao;
		this.credentialDao = credentialDao;
		this.secretCredentialDao = secretCredentialDao;
		this.lastUsedByAccount = new ConcurrentHashMap<>();
		this.lastUsedByCredential = new ConcurrentHashMap<>();
		this.lastUsedBySecretCredential = new ConcurrentHashMap<>();
	}

	public void updateLastUsedDateForCredential(DatarouterAccountCredential credential){
		lastUsedByCredential.put(credential.getKey(), new Date());
		lastUsedByAccount.put(new DatarouterAccountKey(credential.getAccountName()), new Date());
	}

	public void updateLastUsedDateForCredential(DatarouterAccountCredentialKey key, String accountName){
		lastUsedByCredential.put(key, new Date());
		lastUsedByAccount.put(new DatarouterAccountKey(accountName), new Date());
	}

	public void updateLastUsedDateForSecretCredential(DatarouterAccountSecretCredentialKey key, String accountName){
		lastUsedBySecretCredential.put(key, new Date());
		lastUsedByAccount.put(new DatarouterAccountKey(accountName), new Date());
	}

	public void flush(){
		//make a copy of keySet() instead of using it directly (because keySet() is backed by the Map)
		var accountKeys = new HashSet<>(lastUsedByAccount.keySet());
		accountDao.scanMulti(accountKeys)
				.each(account -> account.setLastUsed(lastUsedByAccount.get(account.getKey())))
				.flush(accountDao::putMulti);
		//remove all the copied keys from the Map, even if they weren't found/updated in the DB
		accountKeys.forEach(lastUsedByAccount::remove);

		var credentialKeys = new HashSet<>(lastUsedByCredential.keySet());
		credentialDao.scanMulti(credentialKeys)
				.each(credential -> credential.setLastUsed(lastUsedByCredential.get(credential.getKey())))
				.flush(credentialDao::updateMultiIgnore);
		credentialKeys.forEach(lastUsedByCredential::remove);

		var secretCredentialKeys = new HashSet<>(lastUsedBySecretCredential.keySet());
		secretCredentialDao.scanMulti(secretCredentialKeys)
				.each(credential -> credential.setLastUsed(lastUsedBySecretCredential.get(credential.getKey())))
				.flush(secretCredentialDao::updateMultiIgnore);
		secretCredentialKeys.forEach(lastUsedBySecretCredential::remove);
	}

}
