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
package io.datarouter.aws.sqs.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.SqsPhysicalNode;
import io.datarouter.aws.sqs.SqsQueueNameService;
import io.datarouter.aws.sqs.config.DatarouterSqsPlugin.ServiceNameRegistry;
import io.datarouter.plugin.PluginInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.util.string.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;

@Singleton
public class SqsQueueRegistryService{

	@Inject
	private DatarouterNodes nodes;
	@Inject
	private SqsClientManager sqsClientManager;
	@Inject
	private SqsQueueNameService sqsQueueNameService;
	@Inject
	private PluginInjector pluginInjector;
	@Inject
	private ServiceName serviceName;

	public SqsQueuesForClient getSqsQueuesForClient(ClientId clientId){
		SqsClient sqs = sqsClientManager.getAmazonSqs(clientId);
		List<QueueUrlAndName> knownQueueUrlAndNames = Scanner.of(nodes.getPhysicalNodesForClient(clientId.getName()))
				.map(NodeTool::extractSinglePhysicalNode)
				.map(physicalNode -> (SqsPhysicalNode<?,?,?>)physicalNode)
				.map(SqsPhysicalNode::getQueueUrlAndName)
				.map(Supplier::get)
				.list();
		Set<String> knownQueuesUrls = Scanner.of(knownQueueUrlAndNames)
				.map(QueueUrlAndName::queueUrl)
				.collect(HashSet::new);
		var request = ListQueuesRequest.builder().queueNamePrefix(sqsQueueNameService.buildDefaultNamespace()).build();
		var unreferencedQueues = Scanner.of(sqs.listQueues(request).queueUrls())
				.exclude(queueUrl -> checkForUnreferencedQueues(queueUrl, knownQueuesUrls))
				.map(queueUrl -> StringTool.getStringAfterLastOccurrence("/", queueUrl))
				// it can take up to 60 seconds for a deleted queue to stop showing up in the listQueues call,
				// so double-check that the queue actually exists
				.include(queueName -> sqsQueueExists(sqs, queueName))
				.list();
		return new SqsQueuesForClient(knownQueueUrlAndNames, unreferencedQueues);
	}

	private boolean checkForUnreferencedQueues(String queueUrl, Set<String> knownQueuesUrls){
		return Scanner.of(pluginInjector.getInstance(ServiceNameRegistry.KEY).serviceNames)
				.exclude(name -> name.equals(serviceName.get()))
				.anyMatch(serviceName -> queueUrl.contains(serviceName)
						|| knownQueuesUrls.contains(queueUrl));
	}

	private boolean sqsQueueExists(SqsClient sqs, String queueName){
		try{
			var request = GetQueueUrlRequest.builder().queueName(queueName).build();
			sqs.getQueueUrl(request);
			return true;
		}catch(QueueDoesNotExistException e){
			return false;
		}
	}

	public record SqsQueuesForClient(
			List<QueueUrlAndName> knownQueueUrlByName,
			List<String> unreferencedQueues){
	}

}
