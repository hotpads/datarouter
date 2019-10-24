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
package io.datarouter.web.user;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.web.user.databean.DatarouterPermissionRequest;
import io.datarouter.web.user.databean.DatarouterPermissionRequest.DatarouterPermissionRequestFielder;
import io.datarouter.web.user.databean.DatarouterPermissionRequestKey;

@Singleton
public class DatarouterPermissionRequestDao extends BaseDao implements BaseDatarouterPermissionRequestDao{

	public static class DatarouterPermissionRequestDaoParams extends BaseDaoParams{

		public DatarouterPermissionRequestDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<DatarouterPermissionRequestKey,DatarouterPermissionRequest> node;

	@Inject
	public DatarouterPermissionRequestDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterPermissionRequestDaoParams params){
		super(datarouter);
		node = nodeFactory.create(params.clientId, DatarouterPermissionRequest::new,
				DatarouterPermissionRequestFielder::new).buildAndRegister();
	}

	@Override
	public void putMulti(Collection<DatarouterPermissionRequest> databeans){
		node.putMulti(databeans);
	}

	@Override
	public Scanner<DatarouterPermissionRequest> scan(){
		return node.scan();
	}

	@Override
	public Scanner<DatarouterPermissionRequest> scanWithPrefix(DatarouterPermissionRequestKey prefix){
		return node.scanWithPrefix(prefix);
	}

}
