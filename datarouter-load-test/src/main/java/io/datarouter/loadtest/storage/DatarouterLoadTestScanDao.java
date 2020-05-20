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

import io.datarouter.loadtest.service.LoadTestScanDao;
import io.datarouter.loadtest.storage.RandomValue.RandomValueFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;

/**
 * The node needs to be of a SortedStorageReader type, and the client needs to support that type.
 */
@Singleton
public class DatarouterLoadTestScanDao extends BaseDao implements LoadTestScanDao{

	public static class DatarouterLoadTestScanDaoParams extends BaseDaoParams{

		public DatarouterLoadTestScanDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<RandomValueKey,RandomValue> node;

	@Inject
	public DatarouterLoadTestScanDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterLoadTestScanDaoParams params){
		super(datarouter);
		node = nodeFactory.create(params.clientId, RandomValue::new, RandomValueFielder::new)
				.withTableName("LoadTestScan")
				.buildAndRegister();
	}

	@Override
	public Scanner<RandomValue> scan(int batchSize, int limit){
		Config config = new Config()
				.setOutputBatchSize(batchSize)
				.setNoTimeout();
		return node.scan(config)
				.limit(limit);
	}

}
