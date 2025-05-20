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
package io.datarouter.aws.sqs.group.op;

import java.util.List;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.op.BaseSqsPeekMultiOp;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.queue.GroupQueueMessage;
import software.amazon.awssdk.services.sqs.model.Message;

public class SqsGroupPeekMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseSqsPeekMultiOp<PK,D,F,GroupQueueMessage<PK,D>>{

	public SqsGroupPeekMultiOp(
			Config config,
			BaseSqsNode<PK,D,F> sqsNode,
			SqsClientManager sqsClientManager,
			ClientId clientId){
		super(config, sqsNode, sqsClientManager, clientId);
	}

	@Override
	protected List<GroupQueueMessage<PK, D>> extractDatabeans(List<Message> messages){
		return messages.stream()
				.map(message -> {
					List<D> databeans = codec.fromStringMulti(message.body(), fielder, databeanSupplier);
					byte[] receiptHandle = StringCodec.UTF_8.encode(message.receiptHandle());
					return new GroupQueueMessage<>(receiptHandle, databeans);
				})
				.toList();
	}

}
