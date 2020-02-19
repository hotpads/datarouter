/**
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

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

public class SqsQueuesLengthMonitoringJob extends BaseJob{

	private static final String QUEUE_LENGTH_ATTRIBUTE = QueueAttributeName.ApproximateNumberOfMessages.name();
	private static final List<String> QUEUE_LENGTH_ATTRIBUTE_AS_LIST = Collections.singletonList(
			QUEUE_LENGTH_ATTRIBUTE);

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
		getInitializedSqsClients()
				.collect(Collectors.toMap(Function.identity(), this::getSqsNodesForClient))
				.forEach(this::getQueueLengthAndSaveAsMetric);
	}

	private Stream<ClientId> getInitializedSqsClients(){
		return clientInitializationTracker.getInitializedClients().stream()
				.filter(clientId -> datarouterClients.getClientTypeInstance(clientId) instanceof SqsClientType);
	}

	private List<BaseSqsNode<?,?,?>> getSqsNodesForClient(ClientId clientId){
		return datarouterNodes.getPhysicalNodesForClient(clientId.getName()).stream()
				.map(NodeTool::extractSinglePhysicalNode)
				.map(physicalNode -> (BaseSqsNode<?,?,?>)physicalNode)
				.collect(Collectors.toList());
	}

	private void getQueueLengthAndSaveAsMetric(ClientId clientId, List<BaseSqsNode<?,?,?>> sqsNodes){
		sqsNodes.forEach(baseSqsNode -> {
			String queueUrl = baseSqsNode.getQueueUrl().get();
			String queueName = queueUrl.substring(queueUrl.lastIndexOf('/') + 1);
			String queueLengthString = sqsClientManager.getQueueAttributes(clientId, queueUrl,
					QUEUE_LENGTH_ATTRIBUTE_AS_LIST).get(QUEUE_LENGTH_ATTRIBUTE);
			long queueLength = Long.parseLong(queueLengthString);
			metrics.saveSqsQueueLength(queueName, queueLength);
		});
	}

}
