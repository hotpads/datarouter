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
package io.datarouter.gcp.spanner.node.entity;

import java.util.Collection;
import java.util.Collections;

import io.datarouter.gcp.spanner.SpannerClientManager;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.op.entity.write.SpannerEntityDeleteAllOp;
import io.datarouter.gcp.spanner.op.entity.write.SpannerEntityDeleteOp;
import io.datarouter.gcp.spanner.op.entity.write.SpannerEntityPutOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.entity.PhysicalSubEntitySortedMapStorageNode;
import io.datarouter.storage.node.type.index.ManagedNodesHolder;

public class SpannerSubEntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
		extends SpannerSubEntityReaderNode<EK,E,PK,D,F>
implements PhysicalSubEntitySortedMapStorageNode<EK,PK,D,F>{

	public static final String PARTITION_COLUMN_NAME = "prefix";

	public SpannerSubEntityNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			EntityNodeParams<EK,E> entityNodeParams,
			ManagedNodesHolder managedNodesHolder,
			SpannerClientManager clientManager,
			SpannerFieldCodecRegistry spannerFieldCodecRegistry){
		super(params, clientType, entityNodeParams, managedNodesHolder, clientManager, spannerFieldCodecRegistry);
	}

	@Override
	public void delete(PK key, Config config){
		deleteMulti(Collections.singletonList(key), config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		var op = new SpannerEntityDeleteOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				keys,
				config,
				spannerFieldCodecRegistry,
				partitioner);
		op.wrappedCall();
	}

	@Override
	public void deleteAll(Config config){
		var op = new SpannerEntityDeleteAllOp<>(clientManager.getDatabaseClient(getClientId()), getFieldInfo(), config);
		op.wrappedCall();
	}

	@Override
	public void put(D databean, Config config){
		putMulti(Collections.singletonList(databean), config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		var op = new SpannerEntityPutOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				databeans,
				config,
				spannerFieldCodecRegistry,
				partitioner);
		op.wrappedCall();
	}

}
