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
package io.datarouter.aws.sqs.single.op;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.services.sqs.model.Message;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.op.BaseSqsPeekMultiOp;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.WarnOnModifyList;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.queue.QueueMessage;

public class SqsPeekMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseSqsPeekMultiOp<PK,D,F,QueueMessage<PK,D>>{

	public SqsPeekMultiOp(
			Config config,
			BaseSqsNode<PK,D,F> sqsNode,
			SqsClientManager sqsClientManager,
			ClientId clientId){
		super(config, sqsNode, sqsClientManager, clientId);
	}

	@Override
	protected List<QueueMessage<PK,D>> extractDatabeans(List<Message> messages){
		return messages.stream()
				.map(message -> {
					D databean = codec.fromString(message.getBody(), fielder, databeanSupplier);
					byte[] receiptHandle = StringCodec.UTF_8.encode(message.getReceiptHandle());
					Map<String,String> attributes = Scanner.of(message.getMessageAttributes().entrySet())
							.toMap(Entry::getKey, entry ->
									entry.getValue().getStringValue());
					return new QueueMessage<>(receiptHandle, databean, attributes);
				})
				.collect(WarnOnModifyList.deprecatedCollector());
	}

}
