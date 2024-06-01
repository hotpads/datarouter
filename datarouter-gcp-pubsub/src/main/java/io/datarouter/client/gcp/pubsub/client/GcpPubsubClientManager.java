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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedExecutorProvider;
import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.InstantiatingWatchdogProvider;
import com.google.api.gax.rpc.StatusCode;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.monitoring.v3.ListTimeSeriesRequest;
import com.google.monitoring.v3.ListTimeSeriesResponse;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.protobuf.util.Timestamps;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.SeekRequest;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;

import io.datarouter.client.gcp.pubsub.GcpPubsubExecutors.GcpPubsubPublisherExecutor;
import io.datarouter.client.gcp.pubsub.GcpPubsubExecutors.GcpPubsubSubscriberStubExecutor;
import io.datarouter.client.gcp.pubsub.GcpPubsubExecutors.GcpPubsubWatchdogExecutor;
import io.datarouter.client.gcp.pubsub.TopicAndSubscriptionName;
import io.datarouter.client.gcp.pubsub.config.DatarouterGcpPubsubSettingsRoot;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.util.string.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GcpPubsubClientManager extends BaseClientManager{
	private static final Logger logger = LoggerFactory.getLogger(GcpPubsubClientManager.class);

	private static final Duration ACKNOWLEDGEMENT_DEADLINE = Duration.ofMinutes(10);

	@Inject
	private GcpPubsubClientHolder holder;
	@Inject
	private GcpPubsubOptions gcpPubsubOptions;
	@Inject
	private DatarouterGcpPubsubSettingsRoot settingRoot;
	@Inject
	private GcpPubsubSubscriberStubExecutor subscriberStubExecutor;
	@Inject
	private GcpPubsubWatchdogExecutor gcpPubsubWatchdogExecutor;
	@Inject
	private GcpPubsubPublisherExecutor gcpPubsubPublisherExecutor;
	@Inject
	private DatarouterGcpPubSubTransportChannelProviderFactory transportChannelProviderFactory;

	@Override
	public void shutdown(ClientId clientId){
		holder.getSubscriptionAdminClientInstances(clientId).close();
		holder.getTopicAdminClientInstances(clientId).close();
		holder.getSubscriberStubs(clientId).close();
		holder.getPublisherStubs(clientId).values().forEach(Publisher::shutdown);
		holder.getMetricServiceClient(clientId).shutdown();
	}

	@Override
	protected void safeInitClient(ClientId clientId){
		CredentialsProvider credentialsProvider = gcpPubsubOptions.getCredentialProvider(clientId.getName());
		TopicAdminClient topicAdminClient;
		try{
			TopicAdminSettings topicAdminSettings = TopicAdminSettings.newBuilder()
					.setCredentialsProvider(credentialsProvider)
					.setWatchdogProvider(InstantiatingWatchdogProvider.create()
							.withExecutor(gcpPubsubWatchdogExecutor))
					.setTransportChannelProvider(transportChannelProviderFactory.make(1))
					.build();
			topicAdminClient = TopicAdminClient.create(topicAdminSettings);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		SubscriptionAdminClient subscriptionAdminClient;
		try{
			SubscriptionAdminSettings subscriptionAdminSettings = SubscriptionAdminSettings.newBuilder()
					.setCredentialsProvider(credentialsProvider)
					.setWatchdogProvider(InstantiatingWatchdogProvider.create()
							.withExecutor(gcpPubsubWatchdogExecutor))
					.setTransportChannelProvider(transportChannelProviderFactory.make(1))
					.build();
			subscriptionAdminClient = SubscriptionAdminClient.create(subscriptionAdminSettings);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		SubscriberStub subscriber;
		try{
			SubscriberStubSettings subscriberStubSettings = SubscriberStubSettings.newBuilder()
					.setTransportChannelProvider(transportChannelProviderFactory.make(16))
					.setCredentialsProvider(credentialsProvider)
					.setBackgroundExecutorProvider(FixedExecutorProvider.create(subscriberStubExecutor))
					.setStreamWatchdogProvider(InstantiatingWatchdogProvider.create()
							.withExecutor(gcpPubsubWatchdogExecutor))
					.build();
			subscriber = GrpcSubscriberStub.create(subscriberStubSettings);
		}catch(IOException e){
			throw new RuntimeException(e);
		}

		MetricServiceSettings metricServiceSettings;
		MetricServiceClient metricServiceClient;
		try{
			metricServiceSettings = MetricServiceSettings.newBuilder()
					.setCredentialsProvider(credentialsProvider)
					.setWatchdogProvider(InstantiatingWatchdogProvider.create()
							.withExecutor(gcpPubsubWatchdogExecutor))
					.setTransportChannelProvider(transportChannelProviderFactory.make(1))
					.build();
			metricServiceClient = MetricServiceClient.create(metricServiceSettings);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		holder.register(clientId, subscriptionAdminClient, topicAdminClient, subscriber, metricServiceClient);
	}

	public void peekOnSubscriptionCreation(SubscriberStub subscriberStub, Subscription subscription){
		//This method is a way to induce activity and reset the expiration cycle for the subscription
		PullRequest pullRequest = PullRequest.newBuilder()
				.setMaxMessages(1)
				.setSubscription(subscription.getName())
				.setReturnImmediately(settingRoot.returnImmediately.get())
				.build();
		Executors.newSingleThreadExecutor().submit(() -> subscriberStub.pullCallable().call(pullRequest));
	}

	public void seek(ClientId clientId, String queueName){
		Subscription subscription = createSubscriptionAndGetName(queueName, clientId, getTopicName(clientId,
				queueName));
		SeekRequest seekRequest = SeekRequest.newBuilder()
				.setTime(Timestamps.fromMillis(Instant.now().toEpochMilli()))//deletes messages in queue upto given time
				.setSubscription(subscription.getName())
				.build();
		SubscriptionAdminClient subscriptionAdminClient = getSubscriptionAdminClient(clientId);
		subscriptionAdminClient.seek(seekRequest);
	}

	public TopicName createQueueAndGetName(String queueName, ClientId clientId){
		TopicAdminClient topicAdminClient = getTopicAdminClient(clientId);
		TopicName topicName = getTopicName(clientId, queueName);
		try{
			topicAdminClient.getTopic(topicName);
		}catch(ApiException e){
			if(e.getStatusCode().getCode() == StatusCode.Code.NOT_FOUND){
				topicAdminClient.createTopic(topicName);
				logger.warn("Created topic={}", topicName.toString());
			}else{
				throw e;
			}
		}
		return topicName;
	}

	public Subscription createSubscriptionAndGetName(
			String queueName,
			ClientId clientId,
			TopicName topicName){
		SubscriptionAdminClient subscriptionAdminClient = getSubscriptionAdminClient(clientId);
		SubscriptionName subscriptionName = getProjectSubscriptionName(clientId, queueName);
		Subscription subscription;
		try{
			subscription = subscriptionAdminClient.getSubscription(subscriptionName);
		}catch(ApiException e){
			if(e.getStatusCode().getCode() == StatusCode.Code.NOT_FOUND){
				subscription = subscriptionAdminClient.createSubscription(
						subscriptionName,
						topicName,
						PushConfig.getDefaultInstance(),
						Math.toIntExact(ACKNOWLEDGEMENT_DEADLINE.toSeconds()));
				logger.warn("Created subscription={}", subscription.getName());
			}else{
				throw e;
			}
		}
		return subscription;
	}

	public void createAndRegisterPublisher(ClientId clientId, TopicName topicName){
		Publisher publisher;
		try{
			publisher = Publisher.newBuilder(topicName)
					.setCredentialsProvider(gcpPubsubOptions.getCredentialProvider(clientId.getName()))
					.setExecutorProvider(FixedExecutorProvider.create(gcpPubsubPublisherExecutor))
					.setChannelProvider(transportChannelProviderFactory.make(1))
					.build();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		holder.registerPublisher(clientId, topicName, publisher);
	}

	public SubscriberStub getSubscriber(ClientId clientId){
		return holder.getSubscriberStubs(clientId);
	}

	public Publisher getPublisher(ClientId clientId, TopicName topicName){
		return holder.getPublisherStub(clientId, topicName);
	}

	public SubscriptionAdminClient getSubscriptionAdminClient(ClientId clientId){
		initClient(clientId);
		return holder.getSubscriptionAdminClientInstances(clientId);
	}

	public TopicName getTopicName(ClientId clientId, String queueName){
		return TopicName.of(gcpPubsubOptions.projectId(clientId.getName()), queueName);
	}

	public TopicAdminClient getTopicAdminClient(ClientId clientId){
		initClient(clientId);
		return holder.getTopicAdminClientInstances(clientId);
	}

	public SubscriptionName getProjectSubscriptionName(ClientId clientId, String queueName){
		return SubscriptionName.newBuilder()
				.setProject(gcpPubsubOptions.projectId(clientId.getName()))
				.setSubscription("s-" + queueName)
				.build();
	}

	public GcpPubsubMetricDto getGcpMetricDto(TopicAndSubscriptionName topicAndSubscriptionName, ClientId clientId){
		String topicName = StringTool.getStringAfterLastOccurrence("topics/", topicAndSubscriptionName.topic()
				.toString());
		String subscriptionName = StringTool.getStringAfterLastOccurrence("subscriptions/",
				topicAndSubscriptionName.subscription());
		ProjectName name = ProjectName.of(gcpPubsubOptions.projectId(clientId.getName()));
		long startMillis = Instant.now()
				.minus(Duration.ofMinutes(20))
				.toEpochMilli();
		TimeInterval interval = TimeInterval.newBuilder()
				.setStartTime(Timestamps.fromMillis(startMillis))
				.setEndTime(Timestamps.fromMillis(System.currentTimeMillis())).build();
		MetricServiceClient metricServiceClient = holder.getMetricServiceClient(clientId);
		ListTimeSeriesResponse numUndeliveredMessageResponse = getListTimeSeriesResponse(name, subscriptionName,
				interval, "num_undelivered_messages", metricServiceClient);
		ListTimeSeriesResponse unackedMessageAgeResponse = getListTimeSeriesResponse(name, subscriptionName, interval,
				"oldest_unacked_message_age", metricServiceClient);
		List<TimeSeries> numUndeliveredMessageTimeSeriesList = numUndeliveredMessageResponse.getTimeSeriesList();
		List<TimeSeries> unackedMessageAgeinSecondsTimeSeriesList = unackedMessageAgeResponse.getTimeSeriesList();
		Optional<Long> numUndeliveredMessage = Optional.empty();
		Optional<Long> unackedMessageAgeS = Optional.empty();
		if(numUndeliveredMessageTimeSeriesList.isEmpty()){
			logger.warn("no time series found for subscription={} interval={} errorNumUndeliveredMessage={} ",
					subscriptionName,
					interval,
					numUndeliveredMessageResponse.getExecutionErrorsList());
		}else{
			numUndeliveredMessage = Optional.of(numUndeliveredMessageTimeSeriesList
					.getFirst()
					.getPoints(0)
					.getValue()
					.getInt64Value());
		}
		if(unackedMessageAgeinSecondsTimeSeriesList.isEmpty()){
			logger.warn("no time series found for subscription={} interval={} errorUnackedMessageAge={} ",
					subscriptionName,
					interval,
					unackedMessageAgeResponse.getExecutionErrorsList());
		}else{
			unackedMessageAgeS = Optional.of(unackedMessageAgeinSecondsTimeSeriesList
					.getFirst()
					.getPoints(0)
					.getValue()
					.getInt64Value());
		}
		return new GcpPubsubMetricDto(topicName, numUndeliveredMessage, unackedMessageAgeS);
	}

	public ListTimeSeriesResponse getListTimeSeriesResponse(
			ProjectName name,
			String subscriptionName,
			TimeInterval interval,
			String metricTypeName,
			MetricServiceClient metricServiceClient){
		ListTimeSeriesRequest request = ListTimeSeriesRequest.newBuilder()
				.setName(name.toString())
				.setFilter("metric.type=\"pubsub.googleapis.com/subscription/" + metricTypeName + "\""
						+ " AND resource.label.subscription_id=" + subscriptionName
						+ " AND resource.type=\"pubsub_subscription\"")
				.setInterval(interval)
				.build();
		try(var $ = TracerTool.startSpan("gcp metric client " + metricTypeName, TraceSpanGroupType.HTTP)){
			return metricServiceClient.listTimeSeries(request)
					.getPage()
					.getResponse();
		}
	}

	public record GcpPubsubMetricDto(
			String queueName,
			Optional<Long> numUndeliveredMessages,
			Optional<Long> oldestUnackedMessageAgeS){
	}

}
