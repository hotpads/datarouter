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
package io.datarouter.client.gcp.pubsub.node;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.TopicName;

import io.datarouter.bytes.Codec;
import io.datarouter.client.gcp.pubsub.GcpPubSubBlobOpFactory;
import io.datarouter.client.gcp.pubsub.GcpPubsubClientType;
import io.datarouter.client.gcp.pubsub.TopicAndSubscriptionName;
import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.model.databean.EmptyDatabean;
import io.datarouter.model.databean.EmptyDatabean.EmptyDatabeanFielder;
import io.datarouter.model.key.EmptyDatabeanKey;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.PhysicalBlobQueueStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.queue.BlobQueueMessage;
import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.timer.PhaseTimer;

public class GcpPubsubBlobNode<T>
extends BasePhysicalNode<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder>
implements PhysicalBlobQueueStorageNode<T>, GcpPubsubPhysicalNode<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder>{
	private static final Logger logger = LoggerFactory.getLogger(GcpPubsubBlobNode.class);

	//GCP API documentation - //https://cloud.google.com/pubsub/quotas
	//max size of actual request payload after Google code encodes it (10,000,000 bytes)
	public static final int MAX_SERIALIZED_REQUEST_SIZE = 10_000_000;
	public static final int MAX_TOPIC_PLUS_MESSAGE_SIZE = MAX_SERIALIZED_REQUEST_SIZE
			- 10 //max possible length of Google encoding one topic
			- 20; //max possible length of Google encoding one message

	private final NodeParams<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder> params;
	private final Codec<T,byte[]> codec;
	private final GcpPubsubClientManager gcpPubsubClientManager;
	private final EnvironmentName environmentName;
	private final ServiceName serviceName;
	private final ClientId clientId;
	private final Supplier<TopicAndSubscriptionName> topicAndSubscription;
	private final GcpPubSubBlobOpFactory opFactory;

	public GcpPubsubBlobNode(
			NodeParams<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder> params,
			Codec<T,byte[]> codec,
			GcpPubsubClientType clientType,
			GcpPubsubClientManager clientManager,
			EnvironmentName environmentName,
			ServiceName serviceName){
		super(params, clientType);
		this.params = params;
		this.codec = codec;
		this.gcpPubsubClientManager = clientManager;
		this.environmentName = environmentName;
		this.serviceName = serviceName;
		this.clientId = params.getClientId();
		this.topicAndSubscription = SingletonSupplier.of(this::getOrCreateTopic);
		this.opFactory = new GcpPubSubBlobOpFactory(this, clientManager, clientId);
	}

	private TopicAndSubscriptionName getOrCreateTopic(){
		String queueName;
		String namespace = params.getNamespace().orElse(environmentName.get() + "-" + serviceName.get());
		queueName = StringTool.isEmpty(namespace)
				? getFieldInfo().getTableName()
				: namespace + "-" + getFieldInfo().getTableName();
		var timer = new PhaseTimer();
		TopicName topicName = gcpPubsubClientManager.createQueueAndGetName(queueName, clientId);
		timer.add("createTopic");
		Subscription subscription = gcpPubsubClientManager.createSubscriptionAndGetName(queueName, clientId, topicName);
		timer.add("createSubscription");
		gcpPubsubClientManager.createAndRegisterPublisher(clientId, topicName);
		timer.add("createPublisher");
		SubscriberStub subscriber = gcpPubsubClientManager.getSubscriber(clientId);
		timer.add("getSubscriber");
		gcpPubsubClientManager.peekOnSubscriptionCreation(subscriber, subscription);
		timer.add("peekOnSubscriptionCreation");
		logger.warn("nodeName={} {}", getName(), timer);
		return new TopicAndSubscriptionName(topicName, subscription.getName());
	}

	@Override
	public Supplier<TopicAndSubscriptionName> getTopicAndSubscriptionName(){
		return topicAndSubscription;
	}

	@Override
	public boolean getAgeMonitoringStatusForMetricAlert(){
		return params.getAgeMonitoringStatus();
	}

	@Override
	public int getMaxRawDataSize(){
		int topicBytesSize = topicAndSubscription.get().topic().toString().getBytes(StandardCharsets.UTF_8).length;
		return MAX_TOPIC_PLUS_MESSAGE_SIZE - topicBytesSize;
	}

	@Override
	public Codec<T,byte[]> getCodec(){
		return codec;
	}

	@Override
	public void putRaw(byte[] data, Config config){
		opFactory.makePutOp(data).call();
	}

	@Override
	public Optional<BlobQueueMessage<T>> peek(Config config){
		return Optional.ofNullable(opFactory.makePeekOp().call())
				.map(dto -> new BlobQueueMessage<>(dto, getCodec()));
	}

	@Override
	public void ack(byte[] handle, Config config){
		opFactory.makeAckOp(handle).call();
	}

}
