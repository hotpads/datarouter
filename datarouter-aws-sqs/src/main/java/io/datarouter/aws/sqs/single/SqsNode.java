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
package io.datarouter.aws.sqs.single;

import java.util.Collection;
import java.util.List;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.SqsClientType;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.QueueStorage.PhysicalQueueStorageNode;
import io.datarouter.storage.op.scan.queue.PeekMultiUntilEmptyQueueStorageScanner;
import io.datarouter.storage.op.scan.queue.PollUntilEmptyQueueStorageScanner;
import io.datarouter.storage.queue.BaseQueueMessage;
import io.datarouter.storage.queue.QueueMessage;

public class SqsNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseSqsNode<PK,D,F>
implements PhysicalQueueStorageNode<PK,D,F>{

	public SqsNode(
			DatarouterProperties datarouterProperties,
			DatarouterService datarouterService,
			NodeParams<PK,D,F> params,
			SqsClientType sqsClientType,
			SqsClientManager sqsClientManager,
			ClientId clientId){
		super(datarouterProperties, datarouterService, params, sqsClientType, sqsClientManager, clientId);
	}

	/*------------- reader ------------*/

	@Override
	public QueueMessage<PK,D> peek(Config config){
		Config limitedConfig = config.clone().setLimit(1);
		return sqsOpFactory.makePeekMultiOp(limitedConfig).call().stream().findFirst().orElse(null);
	}

	@Override
	public List<QueueMessage<PK, D>> peekMulti(Config config){
		return sqsOpFactory.makePeekMultiOp(config).call();
	}

	@Override
	public Scanner<QueueMessage<PK, D>> peekUntilEmpty(Config config){
		try(var scanner = new PeekMultiUntilEmptyQueueStorageScanner<>(this, config)){
			return scanner.concat(Scanner::of);
		}
	}

	/*------------- writer ------------*/

	@Override
	public void put(D databean, Config config){
		sqsOpFactory.makePutOp(databean, config).call();
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		sqsOpFactory.makePutMultiOp(databeans, config).call();
	}

	/*------------- reader/writer ------------*/

	@Override
	public D poll(Config config){
		QueueMessage<PK,D> message = peek(config);
		if(message == null){
			return null;
		}
		ack(message.getKey(), config);
		return message.getDatabean();
	}

	@Override
	public List<D> pollMulti(Config config){
		List<QueueMessage<PK, D>> messages = peekMulti(config);
		Scanner.of(messages).map(BaseQueueMessage::getKey).flush(keys -> ackMulti(keys, config));
		return Scanner.of(messages).map(QueueMessage::getDatabean).list();
	}

	@Override
	public Scanner<D> pollUntilEmpty(Config config){
		return new PollUntilEmptyQueueStorageScanner<>(this, config);
	}

}
