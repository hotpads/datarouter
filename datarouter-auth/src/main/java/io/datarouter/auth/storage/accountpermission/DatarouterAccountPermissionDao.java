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
package io.datarouter.auth.storage.accountpermission;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermission.DatarouterAccountPermissionFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;

@Singleton
public class DatarouterAccountPermissionDao extends BaseDao implements BaseDatarouterAccountPermissionDao{

	public static class DatarouterAccountPermissionDaoParams extends BaseDaoParams{

		public DatarouterAccountPermissionDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<DatarouterAccountPermissionKey,DatarouterAccountPermission> node;

	@Inject
	public DatarouterAccountPermissionDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterAccountPermissionDaoParams params){
		super(datarouter);
		node = nodeFactory.create(
				params.clientId,
				DatarouterAccountPermission::new,
				DatarouterAccountPermissionFielder::new)
				.buildAndRegister();
	}

	@Override
	public void put(DatarouterAccountPermission databean){
		node.put(databean);
	}

	@Override
	public void deleteWithPrefix(DatarouterAccountPermissionKey prefix){
		node.deleteWithPrefix(prefix);
	}

	@Override
	public void delete(DatarouterAccountPermissionKey key){
		node.delete(key);
	}

	@Override
	public Scanner<DatarouterAccountPermissionKey> scanKeys(){
		return node.scanKeys();
	}

	@Override
	public Scanner<DatarouterAccountPermissionKey> scanKeysWithPrefix(DatarouterAccountPermissionKey prefix){
		return node.scanKeysWithPrefix(prefix);
	}

}
