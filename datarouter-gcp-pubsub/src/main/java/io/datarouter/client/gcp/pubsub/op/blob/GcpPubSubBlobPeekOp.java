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
package io.datarouter.client.gcp.pubsub.op.blob;

import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;
import com.google.pubsub.v1.ReceivedMessage;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.client.gcp.pubsub.PubsubCostCounters;
import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.node.GcpPubsubBlobNode;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.queue.RawBlobQueueMessage;

public class GcpPubSubBlobPeekOp extends GcpPubsubBlobOp<RawBlobQueueMessage>{

	public GcpPubSubBlobPeekOp(
			GcpPubsubBlobNode<?> node,
			GcpPubsubClientManager clientManager,
			ClientId clientId){
		super(node, clientManager, clientId);
	}

	@Override
	protected RawBlobQueueMessage run(){
		SubscriberStub subscriberStub = clientManager.getSubscriber(clientId);
		PullRequest pullRequest = PullRequest.newBuilder()
				.setMaxMessages(1)
				.setSubscription(subscriberId)
				.build();
		PullResponse pullResponse = subscriberStub.pullCallable().call(pullRequest);
		if(pullResponse.getReceivedMessagesCount() == 0){
			return null;
		}
		ReceivedMessage receivedMessage = pullResponse.getReceivedMessages(0);
		byte[] data = receivedMessage.getMessage().getData().toByteArray();
		byte[] receiptHandle = StringCodec.UTF_8.encode(receivedMessage.getAckId());
		PubsubCostCounters.countMessage(receivedMessage);
		return new RawBlobQueueMessage(receiptHandle, data, receivedMessage.getMessage().getAttributesMap());
	}

}
