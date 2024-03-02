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

import com.google.pubsub.v1.ReceivedMessage;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.config.DatarouterGcpPubsubSettingsRoot;
import io.datarouter.client.gcp.pubsub.node.BaseGcpPubsubNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.queue.QueueMessage;

public class GcpPubsubPeekMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseGcpPubsubPeekMultiOp<PK,D,F,QueueMessage<PK,D>>{

	public GcpPubsubPeekMultiOp(
			Config config,
			BaseGcpPubsubNode<PK,D,F> basePubSubNode,
			GcpPubsubClientManager clientManager,
			DatarouterGcpPubsubSettingsRoot settingRoot,
			ClientId clientId){
		super(config, basePubSubNode, clientManager, settingRoot, clientId);
	}

	@Override
	protected List<QueueMessage<PK,D>> extractDatabeans(List<ReceivedMessage> messages){
		return messages.stream()
				.map(message -> {
					D databean = codec.fromString(
							message.getMessage().getData().toStringUtf8(),
							fielder,
							databeanSupplier);
					byte[] receiptHandle = StringCodec.UTF_8.encode(message.getAckId());
					return new QueueMessage<>(receiptHandle, databean, message.getMessage().getAttributesMap());
				})
				.toList();
	}

}
