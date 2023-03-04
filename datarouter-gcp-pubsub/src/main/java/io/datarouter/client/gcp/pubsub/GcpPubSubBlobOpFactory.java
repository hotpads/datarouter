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

import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.node.GcpPubsubBlobNode;
import io.datarouter.client.gcp.pubsub.op.blob.GcpPubSubBlobPeekOp;
import io.datarouter.client.gcp.pubsub.op.blob.GcpPubsubBlobAckOp;
import io.datarouter.client.gcp.pubsub.op.blob.GcpPubsubBlobOp;
import io.datarouter.client.gcp.pubsub.op.blob.GcpPubsubBlobPutOp;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.queue.RawBlobQueueMessage;

public class GcpPubSubBlobOpFactory{

	private final GcpPubsubBlobNode<?> blobNode;
	private final GcpPubsubClientManager clientManager;
	private final ClientId clientId;

	public GcpPubSubBlobOpFactory(
			GcpPubsubBlobNode<?> blobNode,
			GcpPubsubClientManager clientManager,
			ClientId clientId){
		this.blobNode = blobNode;
		this.clientManager = clientManager;
		this.clientId = clientId;
	}

	public GcpPubsubBlobOp<Void> makePutOp(byte[] data){
		return new GcpPubsubBlobPutOp(data, blobNode, clientManager, clientId);
	}

	public GcpPubsubBlobOp<RawBlobQueueMessage> makePeekOp(){
		return new GcpPubSubBlobPeekOp(blobNode, clientManager, clientId);
	}

	public GcpPubsubBlobOp<Void> makeAckOp(byte[] handle){
		return new GcpPubsubBlobAckOp(handle, blobNode, clientManager, clientId);
	}

}
