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
package io.datarouter.aws.sqs.job;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.model.QueueAttributeName;

import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.SqsClientType;
import io.datarouter.aws.sqs.SqsPhysicalNode;
import io.datarouter.aws.sqs.service.QueueUrlAndName;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientInitializationTracker;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.util.DatarouterQueueMetrics;

public class SqsMonitoringJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(SqsMonitoringJob.class);

	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private SqsClientManager sqsClientManager;
	@Inject
	private ClientInitializationTracker clientInitializationTracker;
	@Inject
	private DatarouterQueueMetrics metrics;

	@Override
	public void run(TaskTracker tracker){
		clientInitializationTracker.getInitializedClients().stream()
				.filter(clientId -> datarouterClients.getClientTypeInstance(clientId) instanceof SqsClientType)
				.forEach(clientId -> {
					Collection<PhysicalNode<?,?,?>> nodes = datarouterNodes.getPhysicalNodesForClient(
							clientId.getName());
					List<QueueUrlAndName> queueUrlAndNames = nodes.stream()
							.map(NodeTool::extractSinglePhysicalNode)
							.map(physicalNode -> (SqsPhysicalNode<?,?,?>)physicalNode)
							.peek($ -> tracker.increment())
							.map(SqsPhysicalNode::getQueueUrlAndName)
							.map(Supplier::get)
							.collect(Collectors.toList());
					saveUnackedMessageAgeMetricForQueues(clientId, queueUrlAndNames.stream()
							.map(QueueUrlAndName::queueName)
							.collect(Collectors.toList()));
					queueUrlAndNames.forEach(queueUrlAndName -> {
						try{
							getQueueLengthAndSaveAsMetric(queueUrlAndName, clientId);
						}catch(RuntimeException e){
							logger.warn("failed to get attribute for queue=" + queueUrlAndName.queueName(), e);
						}
					});

				});
	}

	private void saveUnackedMessageAgeMetricForQueues(ClientId clientId, List<String> queueNames){
		sqsClientManager.getApproximateAgeOfOldestUnackedMessageSecondsGroup(clientId, queueNames).entrySet()
				.forEach(entry -> metrics.saveOldestAckMessageAge(entry.getKey(), entry.getValue(),
						SqsClientType.NAME));
	}

	private void getQueueLengthAndSaveAsMetric(QueueUrlAndName queueUrlAndName, ClientId clientId){
		String queueLengthString = sqsClientManager.getQueueAttribute(clientId, queueUrlAndName.queueUrl(),
				QueueAttributeName.ApproximateNumberOfMessages);
		long queueLength = Long.parseLong(queueLengthString);
		metrics.saveQueueLength(queueUrlAndName.queueName(), queueLength, SqsClientType.NAME);
	}

}
