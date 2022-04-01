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
package io.datarouter.gcp.spanner;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.node.SpannerNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.opencensus.adapter.physical.PhysicalIndexedSortedMapStorageOpencensusAdapter;
import io.datarouter.storage.client.imp.DatabeanClientNodeFactory;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.availability.PhysicalIndexedSortedMapStorageAvailabilityAdapterFactory;
import io.datarouter.storage.node.adapter.callsite.physical.PhysicalIndexedSortedMapStorageCallsiteAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalIndexedSortedMapStorageCounterAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalIndexedSortedMapStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalIndexedSortedMapStorageTraceAdapter;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.type.index.ManagedNodesHolder;
import io.datarouter.storage.node.type.physical.PhysicalNode;

@Singleton
public class SpannerClientNodeFactory
implements DatabeanClientNodeFactory{

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
		var node = new SpannerNode<>(
				nodeParams,
				spannerClientType,
				managedNodesHolder,
				spannerClientManager,
				spannerFieldCodecRegistry);
		return new PhysicalIndexedSortedMapStorageCallsiteAdapter<>(
				new PhysicalIndexedSortedMapStorageSanitizationAdapter<>(
				physicalIndexedSortedMapStorageAvailabilityAdapterFactory.create(
				new PhysicalIndexedSortedMapStorageCounterAdapter<>(
				new PhysicalIndexedSortedMapStorageTraceAdapter<>(
				//custom OpencensusAdapter goes inside TraceAdapter
				new PhysicalIndexedSortedMapStorageOpencensusAdapter<>(node))))));
	}

}
