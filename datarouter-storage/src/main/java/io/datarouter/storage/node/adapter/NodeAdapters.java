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
package io.datarouter.storage.node.adapter;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.adapter.callsite.physical.PhysicalIndexedSortedMapStorageCallsiteAdapter;
import io.datarouter.storage.node.adapter.callsite.physical.PhysicalMapStorageCallsiteAdapter;
import io.datarouter.storage.node.adapter.callsite.physical.PhysicalSortedMapStorageCallsiteAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalBlobQueueStorageCounterAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalGroupQueueStorageCounterAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalIndexedSortedMapStorageCounterAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalMapStorageCounterAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalQueueStorageCounterAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalSortedMapStorageCounterAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalTallyStorageCounterAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalBlobQueueStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalGroupQueueStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalIndexedSortedMapStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalMapStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalQueueStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalSortedMapStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalTallyStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalBlobQueueStorageTraceAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalGroupQueueStorageTraceAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalIndexedSortedMapStorageTraceAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalMapStorageTraceAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalQueueStorageTraceAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalSortedMapStorageTraceAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalTallyStorageTraceAdapter;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import io.datarouter.storage.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.PhysicalBlobQueueStorageNode;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.op.raw.GroupQueueStorage.PhysicalGroupQueueStorageNode;
import io.datarouter.storage.node.op.raw.MapStorage.PhysicalMapStorageNode;
import io.datarouter.storage.node.op.raw.QueueStorage.PhysicalQueueStorageNode;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import jakarta.inject.Singleton;

@Singleton
public class NodeAdapters{

	/*-------------- blob ----------------*/

	public PhysicalBlobStorageNode wrapBlobNode(PhysicalBlobStorageNode physicalBlobNode){
		//no blob adapters yet
		return physicalBlobNode;
	}

	/*-------------- databean ----------------*/

	// CallsiteAdapter should be first as it walks up the stack trace
	// Don't bother with other adapters if SanitizationAdapter doesn't pass

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> wrapDatabeanMapNode(PhysicalMapStorageNode<PK,D,F> physicalDatabeanNode){
		return new PhysicalMapStorageCallsiteAdapter<>(
				new PhysicalMapStorageSanitizationAdapter<>(
				new PhysicalMapStorageCounterAdapter<>(
				new PhysicalMapStorageTraceAdapter<>(physicalDatabeanNode))));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> wrapDatabeanSortedNode(PhysicalSortedMapStorageNode<PK,D,F> physicalDatabeanNode){
		return new PhysicalSortedMapStorageCallsiteAdapter<>(
				new PhysicalSortedMapStorageSanitizationAdapter<>(
				new PhysicalSortedMapStorageCounterAdapter<>(
				new PhysicalSortedMapStorageTraceAdapter<>(physicalDatabeanNode))));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> wrapDatabeanIndexedNode(PhysicalIndexedSortedMapStorageNode<PK,D,F> physicalDatabeanNode){
		return new PhysicalIndexedSortedMapStorageCallsiteAdapter<>(
				new PhysicalIndexedSortedMapStorageSanitizationAdapter<>(
				new PhysicalIndexedSortedMapStorageCounterAdapter<>(
				new PhysicalIndexedSortedMapStorageTraceAdapter<>(physicalDatabeanNode))));
	}

	/*-------------- queue ----------------*/

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> wrapQueueNode(PhysicalQueueStorageNode<PK,D,F> physicalQueueNode){
		return new PhysicalQueueStorageSanitizationAdapter<>(
				new PhysicalQueueStorageCounterAdapter<>(
				new PhysicalQueueStorageTraceAdapter<>(physicalQueueNode)));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> wrapGroupQueueNode(PhysicalGroupQueueStorageNode<PK,D,F> physicalQueueNode){
		return new PhysicalGroupQueueStorageSanitizationAdapter<>(
				new PhysicalGroupQueueStorageCounterAdapter<>(
				new PhysicalGroupQueueStorageTraceAdapter<>(physicalQueueNode)));
	}

	public <T> PhysicalBlobQueueStorageNode<T> wrapBlobQueueNode(PhysicalBlobQueueStorageNode<T> physicalQueueNode){
		return new PhysicalBlobQueueStorageSanitizationAdapter<>(
				new PhysicalBlobQueueStorageCounterAdapter<>(
				new PhysicalBlobQueueStorageTraceAdapter<>(physicalQueueNode)));
	}

	/*-------------- tally ----------------*/

	public PhysicalTallyStorageNode wrapTallyNode(PhysicalTallyStorageNode physicalTallyNode){
		return new PhysicalTallyStorageSanitizationAdapter(
				new PhysicalTallyStorageCounterAdapter(
				new PhysicalTallyStorageTraceAdapter(physicalTallyNode)));
	}

}
