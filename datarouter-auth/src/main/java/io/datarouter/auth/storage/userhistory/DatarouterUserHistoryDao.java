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
package io.datarouter.auth.storage.userhistory;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.userhistory.DatarouterUserHistory.DatarouterUserHistoryFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;

@Singleton
public class DatarouterUserHistoryDao extends BaseDao{

	public static class DatarouterUserHistoryDaoParams extends BaseDaoParams{

		public DatarouterUserHistoryDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<DatarouterUserHistoryKey,DatarouterUserHistory> node;

	@Inject
	public DatarouterUserHistoryDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterUserHistoryDaoParams params){
		super(datarouter);
		node = nodeFactory.create(
				params.clientId,
				DatarouterUserHistory::new,
				DatarouterUserHistoryFielder::new)
				.withIsSystemTable(true)
				.buildAndRegister();
	}

	public void put(DatarouterUserHistory databean){
		node.put(databean);
	}

	public void putMulti(Collection<DatarouterUserHistory> databeans){
		node.putMulti(databeans);
	}

	public List<DatarouterUserHistory> getMulti(Collection<DatarouterUserHistoryKey> keys){
		return node.getMulti(keys);
	}

}
