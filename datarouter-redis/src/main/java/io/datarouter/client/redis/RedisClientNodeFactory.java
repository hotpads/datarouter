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
package io.datarouter.client.redis;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.redis.client.RedisClientManager;
import io.datarouter.client.redis.node.RedisNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.imp.BaseClientNodeFactory;
import io.datarouter.storage.client.imp.TallyClientNodeFactory;
import io.datarouter.storage.client.imp.WrappedNodeFactory;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.availability.PhysicalMapStorageAvailabilityAdapterFactory;
import io.datarouter.storage.node.adapter.callsite.physical.PhysicalMapStorageCallsiteAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalMapStorageCounterAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalTallyStorageCounterAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalTallyStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalMapStorageTraceAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalTallyStorageTraceAdapter;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.op.raw.MapStorage.PhysicalMapStorageNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;

@Singleton
public class RedisClientNodeFactory extends BaseClientNodeFactory implements TallyClientNodeFactory{

	@Inject
	private PhysicalMapStorageAvailabilityAdapterFactory physicalMapStorageAvailabilityAdapterFactory;
	@Inject
	private RedisClientType redisClientType;
	@Inject
	private RedisClientManager redisClientManager;
	@Inject
	private RedisNodeFactory redisNodeFactory;

	public class RedisWrappedNodeFactory<
			EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends WrappedNodeFactory<EK,E,PK,D,F,PhysicalMapStorageNode<PK,D,F>>{

		@Override
		public PhysicalMapStorageNode<PK,D,F> createNode(
				EntityNodeParams<EK,E> entityNodeParams,
				NodeParams<PK,D,F> nodeParams){
			return new RedisNode<>(nodeParams, redisClientType, redisClientManager, nodeParams.getClientId());
		}

		@Override
		public List<UnaryOperator<PhysicalMapStorageNode<PK,D,F>>> getAdapters(){
			return Arrays.asList(
					PhysicalMapStorageCounterAdapter::new,
					PhysicalMapStorageTraceAdapter::new,
					physicalMapStorageAvailabilityAdapterFactory::create,
					PhysicalMapStorageCallsiteAdapter::new);
		}

	}

	@Override
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	WrappedNodeFactory<EK,E,PK,D,F,PhysicalMapStorageNode<PK,D,F>> makeWrappedNodeFactory(){
		return new RedisWrappedNodeFactory<>();
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createTallyNode(NodeParams<PK,D,F> nodeParams){
		var node = redisNodeFactory.createTallyNode(nodeParams);
		return new PhysicalTallyStorageTraceAdapter<>(
				new PhysicalTallyStorageCounterAdapter<>(
				new PhysicalTallyStorageSanitizationAdapter<>(node)));
	}

}
