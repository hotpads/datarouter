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
package io.datarouter.auth.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.datarouter.auth.storage.accountpermission.BaseDatarouterAccountPermissionDao;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionKey;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAccountPermissionKeysByPrefixCache{

	private final BaseDatarouterAccountPermissionDao dao;
	private final AtomicReference<Map<DatarouterAccountPermissionKey,List<DatarouterAccountPermissionKey>>> cache;

	@Inject
	public DatarouterAccountPermissionKeysByPrefixCache(BaseDatarouterAccountPermissionDao dao){
		this.dao = dao;
		this.cache = new AtomicReference<>(load());
	}

	public Map<DatarouterAccountPermissionKey,List<DatarouterAccountPermissionKey>> load(){
		return dao.scanKeys().groupBy(DatarouterAccountPermissionKey::getAccountPrefix);
	}

	public void refresh(){
		cache.set(load());
	}

	public List<DatarouterAccountPermissionKey> get(DatarouterAccountPermissionKey prefix){
		return cache.get().get(prefix);
	}

}
