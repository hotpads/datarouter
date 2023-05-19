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

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.TopicName;

import io.datarouter.client.gcp.pubsub.GcpPubSubOpFactory;
import io.datarouter.client.gcp.pubsub.GcpPubsubClientType;
import io.datarouter.client.gcp.pubsub.TopicAndSubscriptionName;
import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.write.QueueStorageWriter;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.queue.QueueMessageKey;
import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.timer.PhaseTimer;

public abstract class BaseGcpPubsubNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements QueueStorageWriter<PK,D>, GcpPubsubPhysicalNode<PK,D,F>{
	private static final Logger logger = LoggerFactory.getLogger(BaseGcpPubsubNode.class);

	//GCP API documentation - //https://cloud.google.com/pubsub/quotas
	//max size of actual request payload after Google code encodes it (10,000,000 bytes)
	public static final int MAX_SERIALIZED_REQUEST_SIZE = 10_000_000;
	public static final int MAX_TOPIC_PLUS_MESSAGE_SIZE = MAX_SERIALIZED_REQUEST_SIZE
			- 10 //max possible length of Google encoding one topic
			- 20; //max possible length of Google encoding one message
	public static final int MAX_MESSAGES_PER_BATCH = 10;

	private final EnvironmentName environmentName;
	private final ServiceName serviceName;
	private final NodeParams<PK,D,F> params;
	private final GcpPubsubClientManager gcpPubsubClientManager;
	private final ClientId clientId;
	private final Supplier<TopicAndSubscriptionName> topicAndSubscription;
	protected final GcpPubSubOpFactory<PK,D,F> gcpPubSubOpFactory;

	public BaseGcpPubsubNode(
			EnvironmentName environmentName,
			ServiceName serviceName,
			NodeParams<PK,D,F> params,
			GcpPubsubClientType clientType,
			GcpPubsubClientManager clientManager,
			ClientId clientId){
		super(params, clientType);
		this.environmentName = environmentName;
		this.serviceName = serviceName;
		this.params = params;
		this.gcpPubsubClientManager = clientManager;
		this.clientId = clientId;
		this.topicAndSubscription = SingletonSupplier.of(this::getOrCreateTopic);
		this.gcpPubSubOpFactory = new GcpPubSubOpFactory<>(this, clientManager, clientId);
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
	public Duration getCustomMessageAgeThreshold(){
		return params.getCustomMessageAgeThreshold();
	}

	@Override
	public void ack(QueueMessageKey key, Config config){
		ackMulti(List.of(key), config);
	}

	@Override
	public void ackMulti(Collection<QueueMessageKey> keys, Config config){
		gcpPubSubOpFactory.makeAckMultiOp(keys, config).call();
	}

}
