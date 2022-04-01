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
package io.datarouter.client.hbase;

import io.datarouter.client.hbase.callback.CountingBatchCallbackFactory;
import io.datarouter.client.hbase.config.DatarouterHBaseExecutors.DatarouterHbaseClientExecutor;
import io.datarouter.client.hbase.node.nonentity.HBaseNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.imp.DatabeanClientNodeFactory;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.NodeAdapters;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.type.physical.PhysicalNode;

public abstract class BaseHBaseClientNodeFactory
implements DatabeanClientNodeFactory{

	private final ClientType<?,?> clientType;
	private final CountingBatchCallbackFactory countingBatchCallbackFactory;
	private final HBaseClientManager hBaseClientManager;
	private final DatarouterHbaseClientExecutor datarouterHbaseClientExecutor;
	private final NodeAdapters nodeAdapters;

	public BaseHBaseClientNodeFactory(
			ClientType<?,?> clientType,
			CountingBatchCallbackFactory countingBatchCallbackFactory,
			HBaseClientManager hBaseClientManager,
			DatarouterHbaseClientExecutor datarouterHbaseClientExecutor,
			NodeAdapters nodeAdapters){
		this.clientType = clientType;
		this.countingBatchCallbackFactory = countingBatchCallbackFactory;
		this.hBaseClientManager = hBaseClientManager;
		this.datarouterHbaseClientExecutor = datarouterHbaseClientExecutor;
		this.nodeAdapters = nodeAdapters;
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
		var node = new HBaseNode<>(
				hBaseClientManager,
				clientType,
				countingBatchCallbackFactory,
				datarouterHbaseClientExecutor,
				entityNodeParams,
				nodeParams);
		return nodeAdapters.wrapDatabeanSortedNode(node);
	}

}
