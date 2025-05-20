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
package io.datarouter.client.gcp.pubsub.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.pubsub.v1.TopicName;

import io.datarouter.storage.client.ClientId;
import jakarta.inject.Singleton;

@Singleton
public class GcpPubsubClientHolder{

	private final Map<ClientId,SubscriptionAdminClient> subscriptionAdminClientInstances = new ConcurrentHashMap<>();
	private final Map<ClientId,TopicAdminClient> topicAdminClientInstances = new ConcurrentHashMap<>();
	private final Map<ClientId,SubscriberStub> subscriberStubs = new ConcurrentHashMap<>();
	private final Map<ClientId,Map<TopicName,Publisher>> publisherStubs = new ConcurrentHashMap<>();
	private final Map<ClientId,MetricServiceClient> metricServiceClientInstance = new ConcurrentHashMap<>();

	public void register(
			ClientId clientId,
			SubscriptionAdminClient subscriptionAdminClientInstance,
			TopicAdminClient topicAdminClientInstance,
			SubscriberStub subscriberStub,
			MetricServiceClient metricServiceClient){
		subscriptionAdminClientInstances.putIfAbsent(clientId, subscriptionAdminClientInstance);
		topicAdminClientInstances.putIfAbsent(clientId, topicAdminClientInstance);
		subscriberStubs.putIfAbsent(clientId, subscriberStub);
		metricServiceClientInstance.put(clientId, metricServiceClient);
	}

	public void registerPublisher(ClientId clientId, TopicName topicName, Publisher publisher){
		publisherStubs.computeIfAbsent(clientId, _ -> new ConcurrentHashMap<>())
				.putIfAbsent(topicName, publisher);
	}

	public void registerMetricServiceClient(ClientId clientId, MetricServiceClient metricServiceClient){
		metricServiceClientInstance.put(clientId, metricServiceClient);
	}

	public MetricServiceClient getMetricServiceClient(ClientId clientId){
		return metricServiceClientInstance.get(clientId);
	}

	public SubscriptionAdminClient getSubscriptionAdminClientInstances(ClientId clientId){
		return subscriptionAdminClientInstances.get(clientId);
	}

	public TopicAdminClient getTopicAdminClientInstances(ClientId clientId){
		return topicAdminClientInstances.get(clientId);
	}

	public SubscriberStub getSubscriberStubs(ClientId clientId){
		return subscriberStubs.get(clientId);
	}

	public Publisher getPublisherStub(ClientId clientId, TopicName topicName){
		return publisherStubs.get(clientId).get(topicName);
	}

	public Map<TopicName,Publisher> getPublisherStubs(ClientId clientId){
		return publisherStubs.getOrDefault(clientId, Map.of());
	}

}
