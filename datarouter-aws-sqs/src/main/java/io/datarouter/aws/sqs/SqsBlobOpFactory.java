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
package io.datarouter.aws.sqs;

import io.datarouter.aws.sqs.blob.SqsBlobNode;
import io.datarouter.aws.sqs.blob.op.SqsBlobAckOp;
import io.datarouter.aws.sqs.blob.op.SqsBlobPeekOp;
import io.datarouter.aws.sqs.blob.op.SqsBlobPutOp;
import io.datarouter.aws.sqs.op.SqsBlobOp;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.queue.RawBlobQueueMessage;

public class SqsBlobOpFactory{

	private final SqsBlobNode<?> node;
	private final SqsClientManager clientManager;
	private final ClientId clientId;

	public SqsBlobOpFactory(SqsBlobNode<?> node, SqsClientManager clientManager, ClientId clientId){
		this.node = node;
		this.clientManager = clientManager;
		this.clientId = clientId;
	}

	public SqsBlobOp<Void> makePutOp(byte[] data, Config config){
		return new SqsBlobPutOp(data, node.getMaxRawDataSize(), config, clientManager, clientId, getQueueUrl());
	}

	public SqsBlobOp<RawBlobQueueMessage> makePeekOp(Config config){
		return new SqsBlobPeekOp(config, clientManager, clientId, getQueueUrl());
	}

	public SqsBlobOp<Void> makeAckOp(byte[] handle, Config config){
		return new SqsBlobAckOp(handle, config, clientManager, clientId, getQueueUrl());
	}

	private String getQueueUrl(){
		return node.getQueueUrlAndName().get().queueUrl();
	}

}
