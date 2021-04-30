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
package io.datarouter.gcp.spanner;

import java.util.List;
import java.util.function.UnaryOperator;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.node.SpannerNode;
import io.datarouter.gcp.spanner.node.entity.SpannerEntityNode;
import io.datarouter.gcp.spanner.node.entity.SpannerSubEntityNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.opencensus.adapter.physical.PhysicalIndexedSortedMapStorageOpencensusAdapter;
import io.datarouter.opencensus.adapter.physical.PhysicalSubEntitySortedMapStorageOpencensusAdapter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientTableNodeNames;
import io.datarouter.storage.client.imp.BaseClientNodeFactory;
import io.datarouter.storage.client.imp.WrappedNodeFactory;
import io.datarouter.storage.client.imp.WrappedSubEntityNodeFactory;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.availability.PhysicalIndexedSortedMapStorageAvailabilityAdapterFactory;
import io.datarouter.storage.node.adapter.availability.PhysicalSubEntitySortedMapStorageAvailabilityAdapterFactory;
import io.datarouter.storage.node.adapter.callsite.physical.PhysicalIndexedSortedMapStorageCallsiteAdapter;
import io.datarouter.storage.node.adapter.callsite.physical.PhysicalSubEntitySortedMapStorageCallsiteAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalIndexedSortedMapStorageCounterAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalSubEntitySortedMapStorageCounterAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalIndexedSortedMapStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalSubEntitySortedMapStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalIndexedSortedMapStorageTraceAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalSubEntitySortedMapStorageTraceAdapter;
import io.datarouter.storage.node.entity.EntityNode;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.entity.PhysicalSubEntitySortedMapStorageNode;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import io.datarouter.storage.node.type.index.ManagedNodesHolder;

@Singleton
public class SpannerClientNodeFactory extends BaseClientNodeFactory{

	@Inject
	private SpannerClientType spannerClientType;
	@Inject
	private ManagedNodesHolder managedNodesHolder;
	@Inject
	private SpannerClientManager spannerClientManager;
	@Inject
	private SpannerFieldCodecRegistry spannerFieldCodecRegistry;
	@Inject
	private PhysicalIndexedSortedMapStorageAvailabilityAdapterFactory
			physicalIndexedSortedMapStorageAvailabilityAdapterFactory;
	@Inject
	private PhysicalSubEntitySortedMapStorageAvailabilityAdapterFactory
			physicalSubEntitySortedMapStorageAvailabilityAdapterFactory;

	public class SpannerWrappedNodeFactory<
			EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
			extends WrappedNodeFactory<EK,E,PK,D,F,PhysicalIndexedSortedMapStorageNode<PK,D,F>>{

		@Override
		protected PhysicalIndexedSortedMapStorageNode<PK,D,F> createNode(
				EntityNodeParams<EK,E> entityNodeParams,
				NodeParams<PK,D,F> nodeParams){
			return new SpannerNode<>(
					nodeParams,
					spannerClientType,
					managedNodesHolder,
					spannerClientManager,
					spannerFieldCodecRegistry);
		}

		@Override
		protected List<UnaryOperator<PhysicalIndexedSortedMapStorageNode<PK,D,F>>> getAdapters(){
			return List.of(
					PhysicalIndexedSortedMapStorageSanitizationAdapter::new,
					PhysicalIndexedSortedMapStorageCounterAdapter::new,
					PhysicalIndexedSortedMapStorageOpencensusAdapter::new,
					PhysicalIndexedSortedMapStorageTraceAdapter::new,
					physicalIndexedSortedMapStorageAvailabilityAdapterFactory::create,
					PhysicalIndexedSortedMapStorageCallsiteAdapter::new);
		}
	}

	public class SpannerWrappedSubEntityNodeFactory<
			EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends WrappedSubEntityNodeFactory<EK,E,PK,D,F,PhysicalSubEntitySortedMapStorageNode<EK,PK,D,F>>{

		@Override
		protected PhysicalSubEntitySortedMapStorageNode<EK,PK,D,F> createSubEntityNode(
				EntityNodeParams<EK,E> entityNodeParams,
				NodeParams<PK,D,F> nodeParams){
			return new SpannerSubEntityNode<>(nodeParams,
					spannerClientType,
					entityNodeParams,
					managedNodesHolder,
					spannerClientManager,
					spannerFieldCodecRegistry);
		}

		@Override
		protected List<UnaryOperator<PhysicalSubEntitySortedMapStorageNode<EK,PK,D,F>>> getAdapters(){
			return List.of(
					PhysicalSubEntitySortedMapStorageSanitizationAdapter::new,
					PhysicalSubEntitySortedMapStorageCounterAdapter::new,
					PhysicalSubEntitySortedMapStorageOpencensusAdapter::new,
					PhysicalSubEntitySortedMapStorageTraceAdapter::new,
					physicalSubEntitySortedMapStorageAvailabilityAdapterFactory::create,
					PhysicalSubEntitySortedMapStorageCallsiteAdapter::new);
		}

	}

	@Override
	protected <
			EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	WrappedNodeFactory<EK,E,PK,D,F,?> makeWrappedNodeFactory(){
		return new SpannerWrappedNodeFactory<>();
	}

	@Override
	protected <
			EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	WrappedSubEntityNodeFactory<EK,E,PK,D,F,?> makeWrappedSubEntityNodeFactory(){
		return new SpannerWrappedSubEntityNodeFactory<>();
	}

	@Override
	public <EK extends EntityKey<EK>, E extends Entity<EK>> EntityNode<EK,E> createEntityNode(
			NodeFactory nodeFactory, EntityNodeParams<EK,E> entityNodeParams, ClientId clientId){
		ClientTableNodeNames clientTableNodeNames = new ClientTableNodeNames(
				clientId,
				entityNodeParams.getEntityTableName(),
				entityNodeParams.getNodeName());
		return new SpannerEntityNode<>(entityNodeParams, clientTableNodeNames, spannerClientManager,
				spannerFieldCodecRegistry, clientId);
	}

}
