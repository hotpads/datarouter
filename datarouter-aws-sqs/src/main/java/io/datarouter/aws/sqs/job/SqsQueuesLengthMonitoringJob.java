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

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.SqsClientType;
import io.datarouter.aws.sqs.SqsMetrics;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientInitializationTracker;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.util.tuple.Twin;

public class SqsQueuesLengthMonitoringJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(SqsQueuesLengthMonitoringJob.class);

	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private SqsClientManager sqsClientManager;
	@Inject
	private ClientInitializationTracker clientInitializationTracker;
	@Inject
	private SqsMetrics metrics;
	@Inject
	private SqsClientType sqsClientType;

	@Override
	public void run(TaskTracker tracker){
		List<BaseSqsNode<?,?,?>> queues = clientInitializationTracker.getInitializedClients().stream()
				.filter(clientId -> datarouterClients.getClientTypeInstance(clientId) instanceof SqsClientType)
				.map(ClientId::getName)
				.map(datarouterNodes::getPhysicalNodesForClient)
				.flatMap(Collection::stream)
				.map(NodeTool::extractSinglePhysicalNode)
				.map(physicalNode -> (BaseSqsNode<?,?,?>)physicalNode)
				.collect(Collectors.toList());

		List<String> queueNames = queues.stream()
				.map(BaseSqsNode::getQueueUrlAndName)
				.map(Supplier::get)
				.map(Twin::getRight)
				.collect(Collectors.toList());
		saveUnackedMessageAgeMetricForQueues(queueNames);

		queues.stream()
				.peek($ -> tracker.increment())
				.forEach(queue -> {
					try{
						getQueueLengthAndSaveAsMetric(queue);
					}catch(RuntimeException e){
						logger.warn("failed to get attribute for queue=" + queue, e);
					}
				});
	}

	private void saveUnackedMessageAgeMetricForQueues(List<String> queueNames){
		ClientId clientId = ClientId.writer(sqsClientType.getName(), true);
		sqsClientManager.getApproximateAgeOfOldestUnackedMessageSecondsGroup(clientId, queueNames).entrySet()
				.forEach(entry -> metrics.saveUnackedMessageAge(entry.getKey(), entry.getValue()));
	}

	private void getQueueLengthAndSaveAsMetric(BaseSqsNode<?,?,?> baseSqsNode){
		Twin<String> queueUrlAndName = baseSqsNode.getQueueUrlAndName().get();
		String queueName = queueUrlAndName.getRight();
		ClientId clientId = baseSqsNode.getClientId();
		String queueUrl = queueUrlAndName.getLeft();
		String queueLengthString = sqsClientManager.getQueueAttribute(clientId, queueUrl,
				QueueAttributeName.ApproximateNumberOfMessages);
		long queueLength = Long.parseLong(queueLengthString);
		metrics.saveSqsQueueLength(queueName, queueLength);
	}

}
