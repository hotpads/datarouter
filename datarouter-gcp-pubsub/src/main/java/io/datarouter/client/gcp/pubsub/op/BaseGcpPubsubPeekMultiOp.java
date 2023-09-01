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
package io.datarouter.client.gcp.pubsub.op;

import java.util.List;

import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;
import com.google.pubsub.v1.ReceivedMessage;

import io.datarouter.client.gcp.pubsub.PubsubCostCounters;
import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.node.BaseGcpPubsubNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;

public abstract class BaseGcpPubsubPeekMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		T>
extends GcpPubsubOp<PK,D,F,List<T>>{

	public BaseGcpPubsubPeekMultiOp(
			Config config,
			BaseGcpPubsubNode<PK,D,F> basePubSubNode,
			GcpPubsubClientManager clientManager,
			ClientId clientId){
		super(config, basePubSubNode, clientManager, clientId);
	}

	@Override
	protected final List<T> run(){
		SubscriberStub subscriberStub = clientManager.getSubscriber(clientId);
		PullRequest pullRequest = PullRequest.newBuilder()
				.setMaxMessages(config.findLimit().orElse(BaseGcpPubsubNode.MAX_MESSAGES_PER_BATCH))
				.setSubscription(subscriberId)
				.build();
		try{
			PullResponse pullResponse = subscriberStub.pullCallable().call(pullRequest);
			List<ReceivedMessage> receivedMessages = pullResponse.getReceivedMessagesList();
			receivedMessages.forEach(PubsubCostCounters::countMessage);
			return extractDatabeans(receivedMessages);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	protected abstract List<T> extractDatabeans(List<ReceivedMessage> messages);

}
