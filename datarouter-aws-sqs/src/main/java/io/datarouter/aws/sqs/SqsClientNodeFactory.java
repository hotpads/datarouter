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
package io.datarouter.aws.sqs;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.aws.sqs.group.SqsGroupNode;
import io.datarouter.aws.sqs.single.SqsNode;
import io.datarouter.bytes.Codec;
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

@Singleton
public class SqsClientNodeFactory
implements QueueClientNodeFactory, BlobQueueClientNodeFactory{

	@Inject
	private SqsNodeFactory sqsNodeFactory;
	@Inject
	private NodeAdapters nodeAdapters;

	/*-------------- QueueClientNodeFactory --------------*/

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createSingleQueueNode(NodeParams<PK,D,F> nodeParams){
		SqsNode<PK,D,F> node = sqsNodeFactory.createSingleNode(nodeParams);
		return nodeAdapters.wrapQueueNode(node);
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createGroupQueueNode(NodeParams<PK,D,F> nodeParams){
		SqsGroupNode<PK,D,F> node = sqsNodeFactory.createGroupNode(nodeParams);
		return nodeAdapters.wrapGroupQueueNode(node);
	}

	@Override
	public <T> PhysicalBlobQueueStorageNode<T> createBlobQueueNode(
			NodeParams<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder> nodeParams,
			Codec<T,byte[]> codec){
		var node = sqsNodeFactory.createBlobQueueNode(nodeParams, codec);
		return nodeAdapters.wrapBlobQueueNode(node);
	}

}
