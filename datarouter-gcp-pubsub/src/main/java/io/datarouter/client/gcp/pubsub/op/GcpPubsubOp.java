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
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.google.protobuf.ByteString;
import com.google.pubsub.v1.TopicName;

import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.node.BaseGcpPubsubNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.StringDatabeanCodec;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public abstract class GcpPubsubOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		V>
implements Callable<V>{

	protected final Config config;
	protected final String subscriberId;
	protected final TopicName topicId;
	protected final Supplier<D> databeanSupplier;
	protected final F fielder;
	protected final StringDatabeanCodec codec;
	protected final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	protected final GcpPubsubClientManager clientManager;
	protected final ClientId clientId;

	public GcpPubsubOp(
			Config config,
			BaseGcpPubsubNode<PK,D,F> basePubSubNode,
			GcpPubsubClientManager gcpPubsubClientManager,
			ClientId clientId){
		this.config = config;
		this.subscriberId = basePubSubNode.getTopicAndSubscriptionName().get().subscription();
		this.topicId = basePubSubNode.getTopicAndSubscriptionName().get().topic();
		this.databeanSupplier = basePubSubNode.getFieldInfo().getDatabeanSupplier();
		this.fielder = basePubSubNode.getFieldInfo().getSampleFielder();
		this.codec = fielder.getStringDatabeanCodec();
		this.fieldInfo = basePubSubNode.getFieldInfo();
		this.clientManager = gcpPubsubClientManager;
		this.clientId = clientId;
	}

	@Override
	public V call(){
		return run();
	}

	protected abstract V run();

	protected boolean isPutRequestTooBig(ByteString data){
		return isPutRequestTooBig(data.size());
	}

	protected boolean isPutRequestTooBig(byte[] data){
		return isPutRequestTooBig(data.length);
	}

	private boolean isPutRequestTooBig(int messageBytesLength){
		int topicBytesLength = topicId.toString().getBytes(StandardCharsets.UTF_8).length;
		return messageBytesLength + topicBytesLength > BaseGcpPubsubNode.MAX_TOPIC_PLUS_MESSAGE_SIZE;
	}

}
