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
package io.datarouter.aws.sqs;

import java.util.Collection;
import java.util.List;

import io.datarouter.aws.sqs.group.op.SqsGroupPeekMultiOp;
import io.datarouter.aws.sqs.group.op.SqsGroupPutMultiOp;
import io.datarouter.aws.sqs.op.SqsAckMultiOp;
import io.datarouter.aws.sqs.op.SqsAckOp;
import io.datarouter.aws.sqs.op.SqsOp;
import io.datarouter.aws.sqs.single.op.SqsPeekMultiOp;
import io.datarouter.aws.sqs.single.op.SqsPutMultiOp;
import io.datarouter.aws.sqs.single.op.SqsPutOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.queue.GroupQueueMessage;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.queue.QueueMessageKey;

public class SqsOpFactory<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>{

	private final BaseSqsNode<PK,D,F> sqsNode;
	private final SqsClientManager sqsClientManager;
	private final ClientId clientId;

	public SqsOpFactory(BaseSqsNode<PK,D,F> sqsNode, SqsClientManager sqsClientManager, ClientId clientId){
		this.sqsNode = sqsNode;
		this.sqsClientManager = sqsClientManager;
		this.clientId = clientId;
	}

	public SqsOp<PK,D,F,List<QueueMessage<PK,D>>> makePeekMultiOp(Config config){
		return new SqsPeekMultiOp<>(config, sqsNode, sqsClientManager, clientId);
	}

	public SqsOp<PK,D,F,Void> makeAckMultiOp(Collection<QueueMessageKey> keys, Config config){
		return new SqsAckMultiOp<>(keys, config, sqsNode, sqsClientManager, clientId);
	}

	public SqsOp<PK,D,F,Void> makePutMultiOp(Collection<D> databeans, Config config){
		return new SqsPutMultiOp<>(databeans, config, sqsNode, sqsClientManager, clientId);
	}

	public SqsOp<PK,D,F,Void> makePutOp(D databean, Config config){
		return new SqsPutOp<>(databean, config, sqsNode, sqsClientManager, clientId);
	}

	public SqsOp<PK,D,F,Void> makeAckOp(QueueMessageKey key, Config config){
		return new SqsAckOp<>(key, config, sqsNode, sqsClientManager, clientId);
	}

	public SqsOp<PK,D,F,Void> makeGroupPutMultiOp(Collection<D> databeans, Config config){
		return new SqsGroupPutMultiOp<>(databeans, config, sqsNode, sqsClientManager, clientId);
	}

	public SqsOp<PK,D,F,List<GroupQueueMessage<PK,D>>> makeGroupPeekMultiOp(Config config){
		return new SqsGroupPeekMultiOp<>(config, sqsNode, sqsClientManager, clientId);
	}

}
