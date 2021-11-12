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
package io.datarouter.client.memcached.client.nodefactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.memcached.MemcachedClientType;
import io.datarouter.client.memcached.client.MemcachedClientManager;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.Pathbean.PathbeanFielder;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.availability.PhysicalMapStorageAvailabilityAdapterFactory;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalTallyStorageCounterAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalTallyStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalTallyStorageTraceAdapter;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;

@Singleton
public class MemcachedClientNodeFactory extends BaseMemcachedClientNodeFactory{

	private final MemcachedNodeFactory memcachedNodeFactory;

	@Inject
	public MemcachedClientNodeFactory(
			PhysicalMapStorageAvailabilityAdapterFactory physicalMapStorageAvailabilityAdapterFactory,
			MemcachedClientType memcachedClientType,
			MemcachedClientManager memcachedClientManager,
			MemcachedNodeFactory memcachedNodeFactory){
		super(physicalMapStorageAvailabilityAdapterFactory, memcachedClientType, memcachedClientManager);
		this.memcachedNodeFactory = memcachedNodeFactory;
	}

	@Override
	public PhysicalBlobStorageNode createBlobNode(NodeParams<PathbeanKey,Pathbean,PathbeanFielder> nodeParams){
		return memcachedNodeFactory.createBlobNode(nodeParams);
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createTallyNode(NodeParams<PK,D,F> nodeParams){
		var node = memcachedNodeFactory.createTallyNode(nodeParams);
		return new PhysicalTallyStorageTraceAdapter<>(
				new PhysicalTallyStorageCounterAdapter<>(
				new PhysicalTallyStorageSanitizationAdapter<>(node)));
	}

}
