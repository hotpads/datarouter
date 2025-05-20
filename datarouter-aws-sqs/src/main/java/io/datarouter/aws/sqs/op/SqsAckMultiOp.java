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

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.queue.QueueMessageKey;
import io.datarouter.util.concurrent.UncheckedInterruptedException;
import software.amazon.awssdk.core.exception.AbortedException;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequestEntry;

public class SqsAckMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SqsOp<PK,D,F,Void>{

	private final Collection<QueueMessageKey> keys;
	private final SqsClientManager sqsClientManager;
	private final ClientId clientId;

	public SqsAckMultiOp(
			Collection<QueueMessageKey> keys,
			Config config,
			BaseSqsNode<PK,D,F> sqsNode,
			SqsClientManager sqsClientManager,
			ClientId clientId){
		super(config, sqsNode);
		this.keys = keys;
		this.sqsClientManager = sqsClientManager;
		this.clientId = clientId;
	}

	@Override
	protected Void run(){
		for(List<QueueMessageKey> batch : Scanner.of(keys).batch(BaseSqsNode.MAX_MESSAGES_PER_BATCH).iterable()){
			DeleteMessageBatchRequest deleteRequest = Scanner.of(batch)
					.map(key -> DeleteMessageBatchRequestEntry.builder()
									.id(UUID.randomUUID().toString())
									.receiptHandle(new String(key.getHandle()))
									.build())
					.listTo(deleteEntries -> DeleteMessageBatchRequest.builder()
							.queueUrl(queueUrl)
							.entries(deleteEntries)
							.build());
			try{
				sqsClientManager.getAmazonSqs(clientId).deleteMessageBatch(deleteRequest);
			}catch(AbortedException e){
				throw new UncheckedInterruptedException("", e);
			}
		}
		return null;
	}

}
