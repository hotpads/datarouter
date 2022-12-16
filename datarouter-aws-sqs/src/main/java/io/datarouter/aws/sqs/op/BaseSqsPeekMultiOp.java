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
package io.datarouter.aws.sqs.op;

import java.time.Duration;
import java.util.List;

import com.amazonaws.AbortedException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.util.concurrent.UncheckedInterruptedException;

public abstract class BaseSqsPeekMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		T>
extends SqsOp<PK,D,F,List<T>>{

	private final SqsClientManager sqsClientManager;
	private final ClientId clientId;

	public BaseSqsPeekMultiOp(
			Config config,
			BaseSqsNode<PK,D,F> sqsNode,
			SqsClientManager sqsClientManager,
			ClientId clientId){
		super(config, sqsNode);
		this.sqsClientManager = sqsClientManager;
		this.clientId = clientId;
	}

	@Override
	protected final List<T> run(){
		ReceiveMessageRequest request = makeRequest();
		ReceiveMessageResult result;
		try{
			result = sqsClientManager.getAmazonSqs(clientId).receiveMessage(request);
		}catch(AbortedException e){
			throw new UncheckedInterruptedException("", e);
		}
		List<Message> messages = result.getMessages();
		return messages.isEmpty() ? List.of() : extractDatabeans(messages);
	}

	protected abstract List<T> extractDatabeans(List<Message> messages);

	private ReceiveMessageRequest makeRequest(){
		var request = new ReceiveMessageRequest(queueUrl);

		//waitTime
		Duration configTimeout = config.findTimeout()
				.filter(timeout -> timeout.compareTo(BaseSqsNode.MAX_TIMEOUT) <= 0)
				.orElse(BaseSqsNode.MAX_TIMEOUT);
		request.setWaitTimeSeconds(Math.toIntExact(configTimeout.getSeconds()));//must fit in an int

		//visibility timeout
		long visibilityTimeoutMs = config.getVisibilityTimeoutMsOrUse(BaseSqsNode.DEFAULT_VISIBILITY_TIMEOUT_MS);
		request.setVisibilityTimeout((int)Duration.ofMillis(visibilityTimeoutMs).getSeconds());

		//max messages
		request.setMaxNumberOfMessages(config.findLimit().orElse(BaseSqsNode.MAX_MESSAGES_PER_BATCH));
		request.withMessageAttributeNames("ALL");
		return request;
	}

}
