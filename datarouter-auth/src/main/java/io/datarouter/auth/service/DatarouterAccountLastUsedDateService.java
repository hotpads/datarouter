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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.account.BaseDatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountKey;

@Singleton
public class DatarouterAccountLastUsedDateService{

	private final BaseDatarouterAccountDao dao;
	private final Map<DatarouterAccountKey,Date> lastUsedByAccount;

	@Inject
	public DatarouterAccountLastUsedDateService(BaseDatarouterAccountDao dao){
		this.dao = dao;
		this.lastUsedByAccount = new ConcurrentHashMap<>();
	}

	public void updateLastUsedDate(DatarouterAccountKey datarouterAccountKey){
		lastUsedByAccount.put(datarouterAccountKey, new Date());
	}

	public void flush(){
		List<DatarouterAccount> accounts = dao.getMulti(lastUsedByAccount.keySet());
		accounts.forEach(account -> account.setLastUsed(lastUsedByAccount.remove(account.getKey())));
		dao.putMulti(accounts);
	}

}
