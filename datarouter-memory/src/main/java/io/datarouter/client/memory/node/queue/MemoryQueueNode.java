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
package io.datarouter.client.memory.node.queue;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.QueueStorage.PhysicalQueueStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.queue.QueueMessageKey;

public class MemoryQueueNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalQueueStorageNode<PK,D,F>{

	private static final int MAX_MESSAGES_PER_PEEK = 10;

	private final MemoryQueueCodec<PK,D,F> codec;
	private final MemoryQueueStorage storage;

	public MemoryQueueNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType){
		super(params, clientType);
		codec = new MemoryQueueCodec<>(getFieldInfo());
		storage = new MemoryQueueStorage();
	}

	/*------------- QueueStorage --------------*/

	@Override
	public D poll(Config config){
		throw new UnsupportedOperationException();
	}

	@Override
	public List<D> pollMulti(Config config){
		throw new UnsupportedOperationException();
	}

	@Override
	public Scanner<D> pollUntilEmpty(Config config){
		throw new UnsupportedOperationException();
	}

	/*------------- QueueStorageReader --------------*/

	@Override
	public QueueMessage<PK,D> peek(Config config){
		long visibilityTimeoutMs = config.findVisibilityTimeoutMs()
				.orElse(MemoryQueueStorage.DEFAULT_VISIBILITY_TIMEOUT_MS);
		return Scanner.of(storage.peek(1, visibilityTimeoutMs))
				.map(codec::memoryMessageToQueueMessage)
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<QueueMessage<PK,D>> peekMulti(Config config){
		long visibilityTimeoutMs = config.findVisibilityTimeoutMs()
				.orElse(MemoryQueueStorage.DEFAULT_VISIBILITY_TIMEOUT_MS);
		var remainingLimit = new AtomicInteger(config.findLimit().orElseThrow());
		return Scanner.generate(() -> storage.peek(
						Math.min(remainingLimit.get(), MAX_MESSAGES_PER_PEEK),
						visibilityTimeoutMs))
				.advanceUntil(List::isEmpty)
				.each(batch -> remainingLimit.addAndGet(-batch.size()))
				.concat(Scanner::of)
				.map(codec::memoryMessageToQueueMessage)
				.list();
	}

	@Override
	public Scanner<QueueMessage<PK,D>> peekUntilEmpty(Config config){
		long visibilityTimeoutMs = config.findVisibilityTimeoutMs()
				.orElse(MemoryQueueStorage.DEFAULT_VISIBILITY_TIMEOUT_MS);
		return Scanner.generate(() -> storage.peek(MAX_MESSAGES_PER_PEEK, visibilityTimeoutMs))
				.advanceUntil(List::isEmpty)
				.concat(Scanner::of)
				.map(codec::memoryMessageToQueueMessage);
	}

	/*------------- QueueStorageWriter --------------*/

	@Override
	public void ack(QueueMessageKey key, Config config){
		Optional.of(key)
				.map(QueueMessageKey::getHandle)
				.map(MemoryQueueCodec::bytesToId)
				.ifPresent(storage::ack);
	}

	@Override
	public void ackMulti(Collection<QueueMessageKey> keys, Config config){
		Scanner.of(keys)
				.map(QueueMessageKey::getHandle)
				.map(MemoryQueueCodec::bytesToId)
				.forEach(storage::ack);
	}

	/*------------- StorageWriter --------------*/

	@Override
	public void put(D databean, Config config){
		Optional.of(databean)
				.map(codec::databeanToMemoryMessage)
				.ifPresent(storage::add);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		Scanner.of(databeans)
				.map(codec::databeanToMemoryMessage)
				.forEach(storage::add);
	}

}
