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
package io.datarouter.auth.cached;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.util.cached.Cached;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class CachedDatarouterAccounts extends Cached<Map<String,DatarouterAccount>>{

	private final DatarouterAccountDao datarouterAccountDao;

	@Inject
	public CachedDatarouterAccounts(DatarouterAccountDao datarouterAccountDao){
		super(1, TimeUnit.MINUTES);
		this.datarouterAccountDao = datarouterAccountDao;
	}

	@Override
	protected Map<String,DatarouterAccount> reload(){
		return datarouterAccountDao.scan()
				.toMap(account -> account.getKey().getAccountName());
	}

}
