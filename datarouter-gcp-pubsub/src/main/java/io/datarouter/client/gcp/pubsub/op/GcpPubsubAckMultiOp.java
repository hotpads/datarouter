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

import java.util.Collection;
import java.util.List;

import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.pubsub.v1.AcknowledgeRequest;

import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.node.BaseGcpPubsubNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.queue.QueueMessageKey;

public class GcpPubsubAckMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends GcpPubsubOp<PK,D,F,Void>{

	private final Collection<QueueMessageKey> keys;

	public GcpPubsubAckMultiOp(
			Collection<QueueMessageKey> keys,
			Config config,
			BaseGcpPubsubNode<PK,D,F> basePubsubNode,
			GcpPubsubClientManager clientManager,
			ClientId clientId){
		super(config, basePubsubNode, clientManager, clientId);
		this.keys = keys;
	}

	@Override
	protected Void run(){
		for(List<QueueMessageKey> batch : Scanner.of(keys).batch(BaseGcpPubsubNode.MAX_MESSAGES_PER_BATCH).iterable()){
			SubscriberStub subscriberStub = clientManager.getSubscriber(clientId);
			List<String> ackIds = Scanner.of(batch)
					.map(QueueMessageKey::getHandle)
					.map(String::new)
					.list();
			AcknowledgeRequest acknowledgeRequest = AcknowledgeRequest.newBuilder()
					.setSubscription(subscriberId)
					.addAllAckIds(ackIds)
					.build();
			subscriberStub.acknowledgeCallable().call(acknowledgeRequest);
		}
		return null;
	}

}
