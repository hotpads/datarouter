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
package io.datarouter.aws.sqs.blob.op;

import java.time.Duration;
import java.util.List;
import java.util.Map.Entry;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.op.SqsBlobOp;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.queue.RawBlobQueueMessage;
import io.datarouter.util.concurrent.UncheckedInterruptedException;
import software.amazon.awssdk.core.exception.AbortedException;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

public class SqsBlobPeekOp extends SqsBlobOp<RawBlobQueueMessage>{

	public SqsBlobPeekOp(Config config, SqsClientManager sqsClientManager, ClientId clientId, String queueUrl){
		super(sqsClientManager, clientId, config, queueUrl);
	}

	@Override
	protected RawBlobQueueMessage run(){
		ReceiveMessageRequest request = makeRequest();
		ReceiveMessageResponse response;
		try{
			response = sqsClientManager.getAmazonSqs(clientId).receiveMessage(request);
		}catch(AbortedException e){
			throw new UncheckedInterruptedException("", e);
		}
		List<Message> messages = response.messages();
		if(messages.isEmpty()){
			return null;
		}

		Message message = messages.getFirst();
		byte[] data = SqsBlobOp.SQS_BLOB_BASE_64_CODEC.decode(message.body());
		byte[] receiptHandle = StringCodec.UTF_8.encode(message.receiptHandle());
		var attributes = Scanner.of(message.messageAttributes().entrySet())
				.toMap(Entry::getKey, entry -> entry.getValue().stringValue());
		return new RawBlobQueueMessage(receiptHandle, data, attributes);
	}

	private ReceiveMessageRequest makeRequest(){
		var request = ReceiveMessageRequest.builder().queueUrl(queueUrl);

		//waitTime
		Duration configTimeout = config.findTimeout()
				.filter(timeout -> timeout.compareTo(BaseSqsNode.MAX_TIMEOUT) <= 0)
				.orElse(BaseSqsNode.MAX_TIMEOUT);
		request.waitTimeSeconds(Math.toIntExact(configTimeout.getSeconds()));//must fit in an int

		//visibility timeout
		long visibilityTimeoutMs = config.findVisibilityTimeoutMs().orElse(BaseSqsNode.DEFAULT_VISIBILITY_TIMEOUT_MS);
		request.visibilityTimeout((int)Duration.ofMillis(visibilityTimeoutMs).getSeconds());

		//max messages
		request.maxNumberOfMessages(1);
		request.messageAttributeNames("ALL");
		return request.build();
	}

}
