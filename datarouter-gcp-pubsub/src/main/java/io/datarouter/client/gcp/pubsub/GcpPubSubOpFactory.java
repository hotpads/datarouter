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

import java.util.Collection;
import java.util.List;

import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.node.BaseGcpPubsubNode;
import io.datarouter.client.gcp.pubsub.op.GcpPubsubAckMultiOp;
import io.datarouter.client.gcp.pubsub.op.GcpPubsubGroupPeekMultiOp;
import io.datarouter.client.gcp.pubsub.op.GcpPubsubGroupPutMultiOp;
import io.datarouter.client.gcp.pubsub.op.GcpPubsubOp;
import io.datarouter.client.gcp.pubsub.op.GcpPubsubPeekMultiOp;
import io.datarouter.client.gcp.pubsub.op.GcpPubsubPutMultiOp;
import io.datarouter.client.gcp.pubsub.op.GcpPubsubPutOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.queue.GroupQueueMessage;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.queue.QueueMessageKey;

public class GcpPubSubOpFactory<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private final BaseGcpPubsubNode<PK,D,F> baseGcpPubsubNode;
	private final GcpPubsubClientManager clientManager;
	private final ClientId clientId;

	public GcpPubSubOpFactory(
			BaseGcpPubsubNode<PK,D,F> baseGcpPubSubNode,
			GcpPubsubClientManager clientManager,
			ClientId clientId){
		this.baseGcpPubsubNode = baseGcpPubSubNode;
		this.clientManager = clientManager;
		this.clientId = clientId;
	}

	public GcpPubsubOp<PK,D,F,List<QueueMessage<PK,D>>> makePeekMultiOp(Config config){
		return new GcpPubsubPeekMultiOp<>(config, baseGcpPubsubNode, clientManager, clientId);
	}

	public GcpPubsubOp<PK,D,F,Void> makeAckMultiOp(Collection<QueueMessageKey> keys, Config config){
		return new GcpPubsubAckMultiOp<>(keys, config, baseGcpPubsubNode, clientManager, clientId);
	}

	public GcpPubsubOp<PK,D,F,Void> makePutMultiOp(Collection<D> databeans, Config config){
		return new GcpPubsubPutMultiOp<>(databeans, config, baseGcpPubsubNode, clientManager, clientId);
	}

	public GcpPubsubOp<PK,D,F,Void> makePutOp(D databean, Config config){
		return new GcpPubsubPutOp<>(databean, config, baseGcpPubsubNode, clientManager, clientId);
	}

	public GcpPubsubOp<PK,D,F,Void> makeGroupPutMultiOp(Collection<D> databeans, Config config){
		return new GcpPubsubGroupPutMultiOp<>(databeans, config, baseGcpPubsubNode, clientManager, clientId);
	}

	public GcpPubsubOp<PK,D,F,List<GroupQueueMessage<PK,D>>> makeGroupPeekMultiOp(Config config){
		return new GcpPubsubGroupPeekMultiOp<>(config, baseGcpPubsubNode, clientManager, clientId);
	}

}
