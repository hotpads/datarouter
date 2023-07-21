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
package io.datarouter.client.gcp.pubsub;

import io.datarouter.bytes.Codec;
import io.datarouter.client.gcp.pubsub.node.GcpPubsubGroupNode;
import io.datarouter.client.gcp.pubsub.node.GcpPubsubNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.EmptyDatabean;
import io.datarouter.model.databean.EmptyDatabean.EmptyDatabeanFielder;
import io.datarouter.model.key.EmptyDatabeanKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.imp.BlobQueueClientNodeFactory;
import io.datarouter.storage.client.imp.QueueClientNodeFactory;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.NodeAdapters;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.PhysicalBlobQueueStorageNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GcpPubsubClientNodeFactory
implements QueueClientNodeFactory, BlobQueueClientNodeFactory{

	@Inject
	private GcpPubsubNodeFactory nodeFactory;
	@Inject
	private NodeAdapters nodeAdapters;

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createSingleQueueNode(NodeParams<PK,D,F> nodeParams){
		GcpPubsubNode<PK,D,F> node = nodeFactory.createSingleNode(nodeParams);
		return nodeAdapters.wrapQueueNode(node);
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createGroupQueueNode(NodeParams<PK,D,F> nodeParams){
		GcpPubsubGroupNode<PK,D,F> node = nodeFactory.createGroupNode(nodeParams);
		return nodeAdapters.wrapGroupQueueNode(node);
	}

	@Override
	public <T> PhysicalBlobQueueStorageNode<T> createBlobQueueNode(
			NodeParams<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder> nodeParams,
			Codec<T,byte[]> codec){
		var node = nodeFactory.createBlobNode(nodeParams, codec);
		return nodeAdapters.wrapBlobQueueNode(node);
	}

}
