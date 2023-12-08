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
package io.datarouter.filesystem.node.queue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import io.datarouter.filesystem.raw.queue.DirectoryQueue;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.StringDatabeanCodec;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.QueueStorage.PhysicalQueueStorageNode;
import io.datarouter.storage.op.scan.queue.PollUntilEmptyQueueStorageScanner;
import io.datarouter.storage.queue.BaseQueueMessage;
import io.datarouter.storage.queue.QueueMessage;

public class DirectoryQueueNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseDirectoryQueueNode<PK,D,F>
implements PhysicalQueueStorageNode<PK,D,F>{

	public DirectoryQueueNode(
			DirectoryQueue directoryQueue,
			NodeParams<PK,D,F> params){
		super(directoryQueue, params);
	}

	/*------------- reader ------------*/

	@Override
	public QueueMessage<PK,D> peek(Config config){
		return directoryQueue.peek()
				.map(directoryQueueMessage -> {
					DatabeanFielder<PK,D> fielder = getFieldInfo().getSampleFielder();
					Supplier<D> databeanSupplier = getFieldInfo().getDatabeanSupplier();
					StringDatabeanCodec codec = fielder.getStringDatabeanCodec();
					D databean = codec.fromString(directoryQueueMessage.getContentUtf8(), fielder, databeanSupplier);
					byte[] receiptHandle = directoryQueueMessage.getIdUtf8Bytes();
					return new QueueMessage<>(receiptHandle, databean, Map.of());
				})
				.orElse(null);

	}

	@Override
	public List<QueueMessage<PK, D>> peekMulti(Config config){
		int limit = config.findLimit().orElse(10);
		return Scanner.generate(() -> peek(config))
				.limit(limit)
				.list();
	}

	@Override
	public Scanner<QueueMessage<PK, D>> peekUntilEmpty(Config config){
		return Scanner.generate(() -> peek(config))
				.advanceUntil(Objects::isNull);
	}

	/*------------- writer ------------*/

	@Override
	public void put(D databean, Config config){
		DatabeanFielder<PK,D> fielder = getFieldInfo().getSampleFielder();
		String content = fielder.getStringDatabeanCodec().toString(databean, fielder);
		directoryQueue.putMessage(content);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		databeans.forEach(databean -> put(databean, config));
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
