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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.account.BaseDatarouterAccountCredentialDao;
import io.datarouter.auth.storage.account.BaseDatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountCredential;
import io.datarouter.auth.storage.account.DatarouterAccountCredentialKey;
import io.datarouter.auth.storage.account.DatarouterAccountKey;

@Singleton
public class DatarouterAccountLastUsedDateService{

	private final BaseDatarouterAccountDao accountDao;
	private final BaseDatarouterAccountCredentialDao credentialDao;
	private final Map<DatarouterAccountKey,Date> lastUsedByAccount;
	private final Map<DatarouterAccountCredentialKey,Date> lastUsedByCredential;

	@Inject
	public DatarouterAccountLastUsedDateService(BaseDatarouterAccountDao accountDao,
			BaseDatarouterAccountCredentialDao credentialDao){
		this.accountDao = accountDao;
		this.credentialDao = credentialDao;
		this.lastUsedByAccount = new ConcurrentHashMap<>();
		this.lastUsedByCredential = new ConcurrentHashMap<>();
	}

	public void updateLastUsedDate(DatarouterAccountCredential credential){
		lastUsedByCredential.put(credential.getKey(), new Date());
		lastUsedByAccount.put(new DatarouterAccountKey(credential.getAccountName()), new Date());
	}

	public void flush(){
		//make a copy of keySet() instead of using it directly (because keySet() is backed by the Map)
		Set<DatarouterAccountKey> accountKeys = new HashSet<>(lastUsedByAccount.keySet());
		accountDao.scanMulti(accountKeys)
				.each(account -> account.setLastUsed(lastUsedByAccount.get(account.getKey())))
				.flush(accountDao::putMulti);
		//remove all the copied keys from the Map, even if they weren't found/updated in the DB
		accountKeys.forEach(lastUsedByAccount::remove);

		Set<DatarouterAccountCredentialKey> credentialKeys = new HashSet<>(lastUsedByCredential.keySet());
		credentialDao.scanMulti(credentialKeys)
				.each(credential -> credential.setLastUsed(lastUsedByCredential.get(credential.getKey())))
				.flush(credentialDao::putMulti);
		credentialKeys.forEach(lastUsedByCredential::remove);
	}

}
