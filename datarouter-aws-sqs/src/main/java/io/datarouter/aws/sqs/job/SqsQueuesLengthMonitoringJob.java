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

import javax.inject.Inject;

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

	@Override
	public void run(TaskTracker tracker){
		clientInitializationTracker.getInitializedClients().stream()
				.filter(clientId -> datarouterClients.getClientTypeInstance(clientId) instanceof SqsClientType)
				.map(ClientId::getName)
				.map(datarouterNodes::getPhysicalNodesForClient)
				.flatMap(Collection::stream)
				.map(NodeTool::extractSinglePhysicalNode)
				.map(physicalNode -> (BaseSqsNode<?,?,?>)physicalNode)
				.peek($ -> tracker.increment())
				.forEach(this::getQueueLengthAndSaveAsMetric);
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
