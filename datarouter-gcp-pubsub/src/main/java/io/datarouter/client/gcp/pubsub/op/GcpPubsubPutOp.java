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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;

import io.datarouter.client.gcp.pubsub.GcpPubsubDataTooLargeException;
import io.datarouter.client.gcp.pubsub.PubsubCostCounters;
import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.config.DatarouterGcpPubsubSettingsRoot;
import io.datarouter.client.gcp.pubsub.node.BaseGcpPubsubNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.FieldGeneratorTool;

public class GcpPubsubPutOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends GcpPubsubOp<PK,D,F,Void>{

	private final D databean;

	public GcpPubsubPutOp(
			D databean,
			Config config,
			BaseGcpPubsubNode<PK,D,F> basePubsubNode,
			GcpPubsubClientManager clientManager,
			DatarouterGcpPubsubSettingsRoot settingRoot,
			ClientId clientId){
		super(config, basePubsubNode, clientManager, settingRoot, clientId);
		this.databean = databean;
	}

	@Override
	protected Void run(){
		List<String> rejectedDatabeans = new ArrayList<>();
		FieldGeneratorTool.generateAndSetValueForFieldIfNecessary(fieldInfo, databean);
		String encodedDatabean = codec.toString(databean, fielder);
		ByteString data = ByteString.copyFromUtf8(encodedDatabean);
		if(isPutRequestTooBig(data)){
			rejectedDatabeans.add(encodedDatabean);
		}
		if(!rejectedDatabeans.isEmpty()){
			throw new GcpPubsubDataTooLargeException(rejectedDatabeans);
		}
		Publisher publisher = clientManager.getPublisher(clientId, topicId);
		PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
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
