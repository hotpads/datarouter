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
package io.datarouter.job.storage.clustertriggerlock;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.job.storage.clustertriggerlock.ClusterTriggerLock.ClusterTriggerLockFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.util.PrimaryKeyVacuum;
import io.datarouter.storage.util.PrimaryKeyVacuum.PrimaryKeyVacuumBuilder;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class DatarouterClusterTriggerLockDao extends BaseDao{

	public static class DatarouterClusterTriggerLockDaoParams extends BaseRedundantDaoParams{

		public DatarouterClusterTriggerLockDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<ClusterTriggerLockKey,ClusterTriggerLock,ClusterTriggerLockFielder> node;

	@Inject
	public DatarouterClusterTriggerLockDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterClusterTriggerLockDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<ClusterTriggerLockKey,ClusterTriggerLock,ClusterTriggerLockFielder> node =
							nodeFactory.create(clientId, ClusterTriggerLock::new, ClusterTriggerLockFielder::new)
							.withIsSystemTable(true)
							.buildAndRegister();
					return node;
					})
				.listTo(RedundantSortedMapStorageNode::new);
		datarouter.register(node);
	}

	public void putAndAcquire(ClusterTriggerLock databean){
		node.put(databean, new Config()
				.setPutMethod(PutMethod.INSERT_OR_BUST)
				.setIgnoreException(true));
	}

	public void delete(ClusterTriggerLockKey key){
		node.delete(key);
	}

	public PrimaryKeyVacuum<ClusterTriggerLockKey> makeVacuum(){
		Duration retentionPeriod = Duration.ofDays(7);
		Date deleteBeforeTime = Date.from(Instant.now().minus(retentionPeriod));
		return new PrimaryKeyVacuumBuilder<>(
				node.scanKeys(),
				key -> key.getTriggerTime().before(deleteBeforeTime),
				node::deleteMulti)
				.build();
	}

}
