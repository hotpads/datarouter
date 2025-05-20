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

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.queue.QueueMessageKey;
import io.datarouter.util.concurrent.UncheckedInterruptedException;
import software.amazon.awssdk.core.exception.AbortedException;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;

public class SqsAckOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SqsOp<PK,D,F,Void>{

	private final QueueMessageKey key;
	private final SqsClientManager sqsClientManager;
	private final ClientId clientId;

	public SqsAckOp(
			QueueMessageKey key,
			Config config,
			BaseSqsNode<PK,D,F> sqsNode,
			SqsClientManager sqsClientManager,
			ClientId clientId){
		super(config, sqsNode);
		this.key = key;
		this.sqsClientManager = sqsClientManager;
		this.clientId = clientId;
	}

	@Override
	protected Void run(){
		String handle = StringCodec.UTF_8.decode(key.getHandle());
		var deleteRequest = DeleteMessageRequest.builder().queueUrl(queueUrl).receiptHandle(handle).build();
		try{
			sqsClientManager.getAmazonSqs(clientId).deleteMessage(deleteRequest);
		}catch(AbortedException e){
			throw new UncheckedInterruptedException("", e);
		}
		return null;
	}

}
