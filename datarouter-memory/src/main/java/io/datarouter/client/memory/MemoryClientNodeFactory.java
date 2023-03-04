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
package io.datarouter.client.memory;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.bytes.Codec;
import io.datarouter.client.memory.node.blob.MemoryBlobNode;
import io.datarouter.client.memory.node.blobqueue.MemoryBlobQueueNode;
import io.datarouter.client.memory.node.databean.MemoryDatabeanNode;
import io.datarouter.client.memory.node.groupqueue.MemoryGroupQueueNode;
import io.datarouter.client.memory.node.queue.MemoryQueueNode;
import io.datarouter.client.memory.node.tally.MemoryTallyNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.EmptyDatabean;
import io.datarouter.model.databean.EmptyDatabean.EmptyDatabeanFielder;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.EmptyDatabeanKey;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.imp.BlobClientNodeFactory;
import io.datarouter.storage.client.imp.BlobQueueClientNodeFactory;
import io.datarouter.storage.client.imp.DatabeanClientNodeFactory;
import io.datarouter.storage.client.imp.QueueClientNodeFactory;
import io.datarouter.storage.client.imp.TallyClientNodeFactory;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.NodeAdapters;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.PhysicalBlobQueueStorageNode;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;

@Singleton
public class MemoryClientNodeFactory
implements
		BlobClientNodeFactory,
		BlobQueueClientNodeFactory,
		DatabeanClientNodeFactory,
		QueueClientNodeFactory,
		TallyClientNodeFactory{

	@Inject
	private MemoryClientType memoryClientType;
	@Inject
	private NodeAdapters nodeAdapters;

	/*---------------- BlobQueueClientNodeFactory ------------------*/

	@Override
	public <T> PhysicalBlobQueueStorageNode<T> createBlobQueueNode(
			NodeParams<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder> nodeParams,
			Codec<T,byte[]> codec){
		var node = new MemoryBlobQueueNode<>(nodeParams, memoryClientType, codec);
		return nodeAdapters.wrapBlobQueueNode(node);
	}

	/*---------------- BlobClientNodeFactory ------------------*/

	@Override
	public PhysicalBlobStorageNode createBlobNode(
			NodeParams<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> nodeParams){
		var node = new MemoryBlobNode(nodeParams, memoryClientType);
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
		var node = new MemoryDatabeanNode<>(nodeParams, memoryClientType);
		return nodeAdapters.wrapDatabeanSortedNode(node);
	}

	/*---------------- QueueClientNodeFactory ------------------*/

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createSingleQueueNode(NodeParams<PK,D,F> nodeParams){
		var node = new MemoryQueueNode<>(nodeParams, memoryClientType);
		return nodeAdapters.wrapQueueNode(node);
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createGroupQueueNode(NodeParams<PK,D,F> nodeParams){
		var node = new MemoryGroupQueueNode<>(nodeParams, memoryClientType);
		return nodeAdapters.wrapGroupQueueNode(node);
	}

	/*---------------- TallyClientNodeFactory ------------------*/

	@Override
	public PhysicalTallyStorageNode createTallyNode(NodeParams<TallyKey,Tally,TallyFielder> nodeParams){
		var node = new MemoryTallyNode(nodeParams, memoryClientType);
		return nodeAdapters.wrapTallyNode(node);
	}

}
