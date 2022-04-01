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
package io.datarouter.client.redis;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.redis.RedisExecutors.RedisBatchOpExecutor;
import io.datarouter.client.redis.client.RedisClientManager;
import io.datarouter.client.redis.node.RedisBlobNode;
import io.datarouter.client.redis.node.RedisDatabeanNode;
import io.datarouter.client.redis.node.RedisTallyNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.imp.BlobClientNodeFactory;
import io.datarouter.storage.client.imp.DatabeanClientNodeFactory;
import io.datarouter.storage.client.imp.TallyClientNodeFactory;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.Pathbean.PathbeanFielder;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.NodeAdapters;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;

@Singleton
public class RedisClientNodeFactory
implements BlobClientNodeFactory, DatabeanClientNodeFactory, TallyClientNodeFactory{

	@Inject
	private RedisClientType redisClientType;
	@Inject
	private RedisClientManager redisClientManager;
	@Inject
	private RedisBatchOpExecutor executor;
	@Inject
	private NodeAdapters nodeAdapters;

	/*---------------- BlobClientNodeFactory ------------------*/

	@Override
	public PhysicalBlobStorageNode createBlobNode(NodeParams<PathbeanKey,Pathbean,PathbeanFielder> nodeParams){
		var node = new RedisBlobNode(
				nodeParams,
				redisClientType,
				redisClientManager.getLazyClient(nodeParams.getClientId()));
		return nodeAdapters.wrapBlobNode(node);
	}

	/*---------------- DatabeanClientNodeFactory ------------------*/

	@Override
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createDatabeanNode(
			EntityNodeParams<EK,E> entityNodeParams,
			NodeParams<PK,D,F> nodeParams){
		var node = new RedisDatabeanNode<>(
				nodeParams,
				redisClientType,
				redisClientManager.getLazyClient(nodeParams.getClientId()),
				executor);
		return nodeAdapters.wrapDatabeanMapNode(node);
	}

	/*---------------- TallyClientNodeFactory ------------------*/

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createTallyNode(NodeParams<PK,D,F> nodeParams){
		var node = new RedisTallyNode<>(
				nodeParams,
				redisClientType,
				redisClientManager.getLazyClient(nodeParams.getClientId()));
		return nodeAdapters.wrapTallyNode(node);
	}

}
