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
package io.datarouter.client.gcp.pubsub.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.gcp.pubsub.GcpPubsubClientType;
import io.datarouter.client.gcp.pubsub.GcpPubsubExecutors.GcpPubsubQueueLengthMonitoringJobExecutor;
import io.datarouter.client.gcp.pubsub.TopicAndSubscriptionName;
import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager.GcpPubsubMetricDto;
import io.datarouter.client.gcp.pubsub.config.DatarouterGcpPubsubPlugin.SharedQueueNameRegistry;
import io.datarouter.client.gcp.pubsub.node.GcpPubsubPhysicalNode;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.plugin.PluginInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientInitializationTracker;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.storage.util.DatarouterQueueMetrics;
import io.datarouter.util.concurrent.ThreadTool;
import io.datarouter.util.number.RandomTool;
import io.datarouter.util.timer.PhaseTimer;
import jakarta.inject.Inject;

public class GcpPubsubQueueMetricMonitoringJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(GcpPubsubQueueMetricMonitoringJob.class);

	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private ClientInitializationTracker clientInitializationTracker;
	@Inject
	private DatarouterQueueMetrics metrics;
	@Inject
	private GcpPubsubClientManager clientManager;
	@Inject
	private GcpPubsubQueueLengthMonitoringJobExecutor executor;
	@Inject
	private ServiceName serviceName;
	@Inject
	private PluginInjector pluginInjector;

	@Override
	public void run(TaskTracker tracker){
		try{
			//This job is executed across all apps every minute adding delay would likely spread out the GCP requests
			ThreadTool.sleep(RandomTool.nextPositiveInt(6) * 1000);
		}catch(InterruptedException e){
			return;
		}
		Scanner.of(clientInitializationTracker.getInitializedClients())
				.include(clientId -> datarouterClients.getClientTypeInstance(clientId) instanceof GcpPubsubClientType)
				.map(ClientId::getName)
				.concatIter(datarouterNodes::getPhysicalNodesForClient)
				.map(NodeTool::extractSinglePhysicalNode)
				.map(physicalNode -> (GcpPubsubPhysicalNode<?,?,?>)physicalNode)
				.advanceUntil($ -> tracker.shouldStop())
				.parallelUnordered(new Threads(executor, executor.getMaximumPoolSize()))
				.forEach(this::saveQueueLengthMetric);
	}

	private void saveQueueLengthMetric(GcpPubsubPhysicalNode<?,?,?> baseGcpPubsubNode){
		String nodeName = baseGcpPubsubNode.getName();
		logger.debug("starting query for {}", nodeName);
		var timer = new PhaseTimer(nodeName);
		ClientId clientId = baseGcpPubsubNode.getClientId();
		timer.add("getClientId");
		TopicAndSubscriptionName topicAndSubscriptionName = baseGcpPubsubNode.getTopicAndSubscriptionName().get();
		timer.add("getTopicAndSubscriptionName");
		String topicName = topicAndSubscriptionName.topic().getTopic();
		var queueOwner = pluginInjector.getInstance(SharedQueueNameRegistry.KEY).queueOwnerByQueueName.get(topicName);
		boolean shouldSave = queueOwner == null || queueOwner.equals(serviceName.get());
		if(shouldSave){
			saveQueueLength(topicAndSubscriptionName, clientId, timer);
			saveOldestAckMessageAge(topicAndSubscriptionName, clientId, timer);
		}
		logger.info("{}", timer);
	}

	private void saveOldestAckMessageAge(
			TopicAndSubscriptionName topicAndSubscriptionName,
			ClientId clientId,
			PhaseTimer timer){
		GcpPubsubMetricDto metricDto = clientManager.getGcpMetricDto(topicAndSubscriptionName, clientId);
		timer.add("getMetric");
		if(metricDto.oldestUnackedMessageAgeS().isPresent()){
			metrics.saveOldestAckMessageAge(
					metricDto.queueName(),
					metricDto.oldestUnackedMessageAgeS().get(),
					GcpPubsubClientType.NAME);
			timer.add("saveGaugeOldestUnackMessageAge");
		}
	}

	private void saveQueueLength(
			TopicAndSubscriptionName topicAndSubscriptionName,
			ClientId clientId,
			PhaseTimer timer){
		GcpPubsubMetricDto metricDto = clientManager.getGcpMetricDto(topicAndSubscriptionName, clientId);
		timer.add("getMetric");
		if(metricDto.numUndeliveredMessages().isPresent()){
			metrics.saveQueueLength(
					metricDto.queueName(),
					metricDto.numUndeliveredMessages().get(),
					GcpPubsubClientType.NAME);
			timer.add("saveGaugeNumUndeliveredMessages");
		}
	}

}
