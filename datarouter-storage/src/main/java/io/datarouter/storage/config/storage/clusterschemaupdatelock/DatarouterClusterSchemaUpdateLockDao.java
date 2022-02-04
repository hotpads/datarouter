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
package io.datarouter.storage.config.storage.clusterschemaupdatelock;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;
import io.datarouter.storage.config.storage.clusterschemaupdatelock.ClusterSchemaUpdateLock.ClusterSchemaUpdateLockFielder;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.tag.Tag;

@Singleton
public class DatarouterClusterSchemaUpdateLockDao extends BaseDao{

	public static class DatarouterClusterSchemaUpdateLockDaoParams extends BaseDaoParams{

		public final Optional<ClientId> optRedundantClientId;

		public DatarouterClusterSchemaUpdateLockDaoParams(List<ClientId> clientIds){
			super(clientIds.get(0));
			this.optRedundantClientId = clientIds.size() > 1 ? Optional.of(clientIds.get(1)) : Optional.empty();
		}

	}

	private final SortedMapStorage<ClusterSchemaUpdateLockKey,ClusterSchemaUpdateLock> mainNode;
	private final Optional<SortedMapStorage<ClusterSchemaUpdateLockKey,ClusterSchemaUpdateLock>> optRedundantNode;

	@Inject
	public DatarouterClusterSchemaUpdateLockDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterClusterSchemaUpdateLockDaoParams params){
		super(datarouter);
		mainNode = nodeFactory.create(params.clientId,
				ClusterSchemaUpdateLock::new,
				ClusterSchemaUpdateLockFielder::new)
				.withTag(Tag.DATAROUTER)
				.buildAndRegister();

		optRedundantNode = params.optRedundantClientId
				.map(clientId -> nodeFactory.create(
						clientId,
						ClusterSchemaUpdateLock::new,
						ClusterSchemaUpdateLockFielder::new)
				.withTag(Tag.DATAROUTER)
				.buildAndRegister());
	}

	public void putAndAcquire(ClusterSchemaUpdateLock databean){
		var config = new Config().setPutMethod(PutMethod.INSERT_OR_BUST).setIgnoreException(true);
		mainNode.put(databean,config);
		optRedundantNode.ifPresent(redundantNode -> redundantNode.put(databean, config));
	}

	public ClusterSchemaUpdateLock get(ClusterSchemaUpdateLockKey key){
		return mainNode.get(key);
	}

	public void delete(ClusterSchemaUpdateLockKey key){
		mainNode.delete(key);
		optRedundantNode.ifPresent(redundantNode -> redundantNode.delete(key));
	}

	public Scanner<ClusterSchemaUpdateLock> scan(){
		return mainNode.scan();
	}

}
