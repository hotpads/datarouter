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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.client.gcp.pubsub.GcpPubsubDataTooLargeException;
import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.node.BaseGcpPubsubNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;

public class GcpPubsubGroupPutMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends GcpPubsubOp<PK,D,F,Void>{

	private final Collection<D> databeans;

	public GcpPubsubGroupPutMultiOp(
			Collection<D> databeans,
			Config config,
			BaseGcpPubsubNode<PK,D,F> basePubSubNode,
			GcpPubsubClientManager clientManager,
			ClientId clientId){
		super(config, basePubSubNode, clientManager, clientId);
		this.databeans = databeans;
	}

	@Override
	protected Void run(){
		List<String> rejectedDatabeans = new ArrayList<>();
		List<byte[]> encodedDatabeans = new ArrayList<>();
		for(D databean : databeans){
			String databeanAsString = codec.toString(databean, fielder);
			byte[] databeanAsBytes = StringCodec.UTF_8.encode(databeanAsString);
			if(isPutRequestTooBig(databeanAsBytes)){
				rejectedDatabeans.add(databeanAsString);
				continue;
			}
			encodedDatabeans.add(databeanAsBytes);
		}
		Publisher publisher = clientManager.getPublisher(clientId, topicId);
		int topicBytesLength = topicId.toString().getBytes(StandardCharsets.UTF_8).length;
		codec.makeGroups(encodedDatabeans, BaseGcpPubsubNode.MAX_TOPIC_PLUS_MESSAGE_SIZE - topicBytesLength)
				.forEach(group -> flush(publisher, group));
		if(!rejectedDatabeans.isEmpty()){
			throw new GcpPubsubDataTooLargeException(rejectedDatabeans);
		}
		return null;
	}

	private void flush(Publisher publisher, List<byte[]> group){
		ByteString data = ByteString.copyFrom(codec.concatGroup(group));
		PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
		ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
		try{
			messageIdFuture.get();
		}catch(InterruptedException | ExecutionException e){
			throw new RuntimeException(e);
		}
	}

}
