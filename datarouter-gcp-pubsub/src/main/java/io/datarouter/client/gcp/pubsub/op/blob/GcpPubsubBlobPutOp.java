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

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;

import io.datarouter.client.gcp.pubsub.GcpPubsubDataTooLargeException;
import io.datarouter.client.gcp.pubsub.PubsubCostCounters;
import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.node.GcpPubsubBlobNode;
import io.datarouter.storage.client.ClientId;

public class GcpPubsubBlobPutOp extends GcpPubsubBlobOp<Void>{

	private final byte[] data;

	public GcpPubsubBlobPutOp(
			byte[] data,
			GcpPubsubBlobNode<?> node,
			GcpPubsubClientManager clientManager,
			ClientId clientId){
		super(node, clientManager, clientId);
		this.data = data;
	}

	@Override
	protected Void run(){
		if(isPutRequestTooBig(data)){
			throw new GcpPubsubDataTooLargeException(List.of("a blob of size " + data.length));
		}
		ByteString byteString = ByteString.copyFrom(data);
		Publisher publisher = clientManager.getPublisher(clientId, topicId);
		PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(byteString).build();
		PubsubCostCounters.countMessage(pubsubMessage);
		ApiFuture<String> future = publisher.publish(pubsubMessage);
		try{
			future.get();
		}catch(InterruptedException | ExecutionException e){
			throw new RuntimeException(e);
		}
		return null;
	}

}
