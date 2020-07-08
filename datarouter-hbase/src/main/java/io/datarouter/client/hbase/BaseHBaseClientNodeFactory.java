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
package io.datarouter.client.hbase;

import java.util.List;
import java.util.function.UnaryOperator;

import io.datarouter.client.hbase.callback.CountingBatchCallbackFactory;
import io.datarouter.client.hbase.config.DatarouterHBaseExecutors.DatarouterHbaseClientExecutor;
import io.datarouter.client.hbase.node.entity.HBaseEntityNode;
import io.datarouter.client.hbase.node.nonentity.HBaseNode;
import io.datarouter.client.hbase.node.subentity.HBaseSubEntityNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientTableNodeNames;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.imp.BaseClientNodeFactory;
import io.datarouter.storage.client.imp.WrappedNodeFactory;
import io.datarouter.storage.client.imp.WrappedSubEntityNodeFactory;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.availability.PhysicalSortedMapStorageAvailabilityAdapterFactory;
import io.datarouter.storage.node.adapter.availability.PhysicalSubEntitySortedMapStorageAvailabilityAdapterFactory;
import io.datarouter.storage.node.adapter.callsite.physical.PhysicalSortedMapStorageCallsiteAdapter;
import io.datarouter.storage.node.adapter.callsite.physical.PhysicalSubEntitySortedMapStorageCallsiteAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalSortedMapStorageCounterAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalSubEntitySortedMapStorageCounterAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalSortedMapStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalSubEntitySortedMapStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalSortedMapStorageTraceAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalSubEntitySortedMapStorageTraceAdapter;
import io.datarouter.storage.node.entity.EntityNode;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.entity.PhysicalSubEntitySortedMapStorageNode;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;

public abstract class BaseHBaseClientNodeFactory extends BaseClientNodeFactory{

	private final ClientType<?,?> clientType;
	private final PhysicalSortedMapStorageAvailabilityAdapterFactory physicalSortedMapStorageAvailabilityAdapterFactory;
	private final PhysicalSubEntitySortedMapStorageAvailabilityAdapterFactory
			physicalSubEntitySortedMapStorageAvailabilityAdapterFactory;
	private final CountingBatchCallbackFactory countingBatchCallbackFactory;
	private final HBaseClientManager hBaseClientManager;
	private final DatarouterHbaseClientExecutor datarouterHbaseClientExecutor;

	public BaseHBaseClientNodeFactory(
			ClientType<?,?> clientType,
			PhysicalSortedMapStorageAvailabilityAdapterFactory physicalSortedMapStorageAvailabilityAdapterFactory,
			PhysicalSubEntitySortedMapStorageAvailabilityAdapterFactory
			physicalSubEntitySortedMapStorageAvailabilityAdapterFactory,
			CountingBatchCallbackFactory countingBatchCallbackFactory,
			HBaseClientManager hBaseClientManager,
			DatarouterHbaseClientExecutor datarouterHbaseClientExecutor){
		this.clientType = clientType;
		this.physicalSortedMapStorageAvailabilityAdapterFactory = physicalSortedMapStorageAvailabilityAdapterFactory;
		this.physicalSubEntitySortedMapStorageAvailabilityAdapterFactory =
				physicalSubEntitySortedMapStorageAvailabilityAdapterFactory;
		this.countingBatchCallbackFactory = countingBatchCallbackFactory;
		this.hBaseClientManager = hBaseClientManager;
		this.datarouterHbaseClientExecutor = datarouterHbaseClientExecutor;
	}

	public class HBaseWrappedNodeFactory<
			EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends WrappedNodeFactory<EK,E,PK,D,F,PhysicalSortedMapStorageNode<PK,D,F>>{

		@Override
		public PhysicalSortedMapStorageNode<PK,D,F> createNode(
				EntityNodeParams<EK,E> entityNodeParams,
				NodeParams<PK,D,F> nodeParams){
			return new HBaseNode<>(hBaseClientManager, clientType, countingBatchCallbackFactory,
					datarouterHbaseClientExecutor, entityNodeParams, nodeParams);
		}

		@Override
		public List<UnaryOperator<PhysicalSortedMapStorageNode<PK,D,F>>> getAdapters(){
			return List.of(
					PhysicalSortedMapStorageSanitizationAdapter::new,
					PhysicalSortedMapStorageCounterAdapter::new,
					PhysicalSortedMapStorageTraceAdapter::new,
					physicalSortedMapStorageAvailabilityAdapterFactory::create,
					PhysicalSortedMapStorageCallsiteAdapter::new);
		}

	}

	public class HBaseWrappedSubEntityNodeFactory<
			EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends WrappedSubEntityNodeFactory<EK,E,PK,D,F,PhysicalSubEntitySortedMapStorageNode<EK,PK,D,F>>{

		@Override
		public PhysicalSubEntitySortedMapStorageNode<EK,PK,D,F> createSubEntityNode(
				EntityNodeParams<EK,E> entityNodeParams,
				NodeParams<PK,D,F> nodeParams){
			return new HBaseSubEntityNode<>(hBaseClientManager, countingBatchCallbackFactory, entityNodeParams,
					nodeParams, clientType, datarouterHbaseClientExecutor);
		}

		@Override
		public List<UnaryOperator<PhysicalSubEntitySortedMapStorageNode<EK,PK,D,F>>> getAdapters(){
			return List.of(
					PhysicalSubEntitySortedMapStorageSanitizationAdapter::new,
					PhysicalSubEntitySortedMapStorageCounterAdapter::new,
					PhysicalSubEntitySortedMapStorageTraceAdapter::new,
					physicalSubEntitySortedMapStorageAvailabilityAdapterFactory::create,
					PhysicalSubEntitySortedMapStorageCallsiteAdapter::new);
		}

	}

	@Override
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	WrappedNodeFactory<EK,E,PK,D,F,?> makeWrappedNodeFactory(){
		return new HBaseWrappedNodeFactory<>();
	}

	@Override
	protected <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	WrappedSubEntityNodeFactory<EK,E,PK,D,F,?> makeWrappedSubEntityNodeFactory(){
		return new HBaseWrappedSubEntityNodeFactory<>();
	}

	@Override
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>>
	EntityNode<EK,E> createEntityNode(
			NodeFactory nodeFactory,
			EntityNodeParams<EK,E> entityNodeParams,
			ClientId clientId){
		ClientTableNodeNames clientTableNodeNames = new ClientTableNodeNames(
				clientId,
				entityNodeParams.getEntityTableName(),
				entityNodeParams.getNodeName());
		return new HBaseEntityNode<>(hBaseClientManager, nodeFactory, entityNodeParams, clientTableNodeNames,
				clientType);
	}

}
