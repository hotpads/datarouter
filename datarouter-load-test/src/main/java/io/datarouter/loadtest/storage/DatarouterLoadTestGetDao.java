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
package io.datarouter.loadtest.storage;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.loadtest.service.LoadTestGetDao;
import io.datarouter.loadtest.storage.RandomValue.RandomValueFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;

/**
 * The node needs to be of a MapStorageReader type, and the client needs to support that type.
 */
@Singleton
public class DatarouterLoadTestGetDao extends BaseDao implements LoadTestGetDao{

	public static class DatarouterLoadTestGetDaoParams extends BaseDaoParams{

		public DatarouterLoadTestGetDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final MapStorage<RandomValueKey,RandomValue> node;

	@Inject
	public DatarouterLoadTestGetDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterLoadTestGetDaoParams params){
		super(datarouter);
		node = nodeFactory.create(params.clientId, RandomValue::new, RandomValueFielder::new)
				.withTableName("LoadTestGet")
				.buildAndRegister();
	}

	@Override
	public MapStorageReader<RandomValueKey,RandomValue> getNode(){
		return node;
	}

}
