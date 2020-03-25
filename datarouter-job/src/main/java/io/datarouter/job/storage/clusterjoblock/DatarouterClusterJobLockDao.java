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
package io.datarouter.job.storage.clusterjoblock;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.job.storage.clusterjoblock.ClusterJobLock.ClusterJobLockFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.util.DatabeanVacuum;
import io.datarouter.storage.util.DatabeanVacuum.DatabeanVacuumBuilder;
import io.datarouter.util.DateTool;

@Singleton
public class DatarouterClusterJobLockDao extends BaseDao{

	public static class DatarouterClusterJobLockDaoParams extends BaseDaoParams{

		public DatarouterClusterJobLockDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<ClusterJobLockKey,ClusterJobLock> node;

	@Inject
	public DatarouterClusterJobLockDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterClusterJobLockDaoParams params){
		super(datarouter);
		node = nodeFactory.create(params.clientId, ClusterJobLock::new, ClusterJobLockFielder::new)
				.buildAndRegister();
	}

	public void putAndAcquire(ClusterJobLock databean){
		node.put(databean, new Config()
				.setPutMethod(PutMethod.INSERT_OR_BUST)
				.setIgnoreException(true));
	}

	public ClusterJobLock get(ClusterJobLockKey key){
		return node.get(key);
	}

	public void delete(ClusterJobLockKey key){
		node.delete(key);
	}

	public Scanner<ClusterJobLock> scan(){
		return node.scan();
	}

	public DatabeanVacuum<ClusterJobLockKey,ClusterJobLock> makeVacuum(){
		return new DatabeanVacuumBuilder<>(
				node.scan(),
				databean -> DateTool.hasPassed(databean.getExpirationTime()),
				node::deleteMulti)
				.build();
	}

}
