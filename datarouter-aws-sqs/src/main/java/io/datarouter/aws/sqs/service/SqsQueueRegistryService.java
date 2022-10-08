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

import javax.inject.Inject;
import javax.inject.Singleton;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;

import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.SqsPhysicalNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.util.string.StringTool;

@Singleton
public class SqsQueueRegistryService{

	@Inject
	private DatarouterNodes nodes;
	@Inject
	private SqsClientManager sqsClientManager;

	public SqsQueuesForClient getSqsQueuesForClient(ClientId clientId){
		Set<String> knownQueuesUrls = new HashSet<>();
		AmazonSQS sqs = sqsClientManager.getAmazonSqs(clientId);
		List<? extends SqsPhysicalNode<?,?,?>> sqsNodes = Scanner.of(
				nodes.getPhysicalNodesForClient(clientId.getName()))
				.map(NodeTool::extractSinglePhysicalNode)
				.map(physicalNode -> (SqsPhysicalNode<?,?,?>)physicalNode)
				.list();
		List<QueueUrlAndName> knownQueueUrlByName = Scanner.of(sqsNodes)
				.map(SqsPhysicalNode::getQueueUrlAndName)
				.map(Supplier::get)
				.each(twin -> knownQueuesUrls.add(twin.queueUrl()))
				.list();
		List<String> unreferencedQueues = Scanner.of(sqsNodes)
				//this intentionally avoids manually specified namespaces, since they could overlap with other services
				.map(SqsPhysicalNode::getAutomaticNamespace)
				.distinct()
				.map(sqs::listQueues)
				.concatIter(ListQueuesResult::getQueueUrls)
				.exclude(knownQueuesUrls::contains)
				.map(queueUrl -> StringTool.getStringAfterLastOccurrence("/", queueUrl))
				.include(queueName -> sqsQueueExists(sqs, queueName))
				.list();
		return new SqsQueuesForClient(knownQueueUrlByName, unreferencedQueues);
	}

	public record SqsQueuesForClient(
			List<QueueUrlAndName> knownQueueUrlByName,
			List<String> unreferencedQueues){
	}

	private boolean sqsQueueExists(AmazonSQS sqs, String queueName){
		try{
			sqs.getQueueUrl(queueName);
			return true;
		}catch(QueueDoesNotExistException e){
			return false;
		}
	}

}
