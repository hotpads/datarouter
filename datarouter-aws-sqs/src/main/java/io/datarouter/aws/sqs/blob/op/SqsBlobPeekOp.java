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

import com.amazonaws.AbortedException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.op.SqsBlobOp;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.queue.RawBlobQueueMessage;
import io.datarouter.util.concurrent.UncheckedInterruptedException;

public class SqsBlobPeekOp extends SqsBlobOp<RawBlobQueueMessage>{

	public SqsBlobPeekOp(Config config, SqsClientManager sqsClientManager, ClientId clientId, String queueUrl){
		super(sqsClientManager, clientId, config, queueUrl);
	}

	@Override
	protected RawBlobQueueMessage run(){
		ReceiveMessageRequest request = makeRequest();
		ReceiveMessageResult result;
		try{
			result = sqsClientManager.getAmazonSqs(clientId).receiveMessage(request);
		}catch(AbortedException e){
			throw new UncheckedInterruptedException("", e);
		}
		List<Message> messages = result.getMessages();
		if(messages.isEmpty()){
			return null;
		}

		Message message = messages.get(0);
		byte[] data = SqsBlobOp.SQS_BLOB_BASE_64_CODEC.decode(message.getBody());
		byte[] receiptHandle = StringCodec.UTF_8.encode(message.getReceiptHandle());
		var attributes = Scanner.of(message.getMessageAttributes().entrySet())
				.toMap(Entry::getKey, entry -> entry.getValue().getStringValue());
		return new RawBlobQueueMessage(receiptHandle, data, attributes);
	}

	private ReceiveMessageRequest makeRequest(){
		var request = new ReceiveMessageRequest(queueUrl);

		//waitTime
		Duration configTimeout = config.findTimeout().orElse(Duration.ofMillis(Long.MAX_VALUE));
		long waitTimeMs = Math.min(configTimeout.toMillis(), BaseSqsNode.MAX_TIMEOUT_SECONDS * 1000);
		request.setWaitTimeSeconds((int)Duration.ofMillis(waitTimeMs).getSeconds());//must fit in an int

		//visibility timeout
		long visibilityTimeoutMs = config.getVisibilityTimeoutMsOrUse(BaseSqsNode.DEFAULT_VISIBILITY_TIMEOUT_MS);
		request.setVisibilityTimeout((int)Duration.ofMillis(visibilityTimeoutMs).getSeconds());

		//max messages
		request.setMaxNumberOfMessages(1);
		request.withMessageAttributeNames("ALL");
		return request;
	}

}
