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
package io.datarouter.client.memory.node.groupqueue;

import java.time.Duration;
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
import io.datarouter.storage.node.op.raw.GroupQueueStorage.PhysicalGroupQueueStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.queue.GroupQueueMessage;
import io.datarouter.storage.queue.QueueMessageKey;

public class MemoryGroupQueueNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalGroupQueueStorageNode<PK,D,F>{

	private static final long DEFAULT_VISIBILITY_TIMEOUT_MS = Duration.ofMinutes(1).toMillis();
	private static final int MAX_MESSAGES_PER_PEEK = 10;

	private final MemoryGroupQueueCodec<PK,D,F> codec;
	private final MemoryGroupQueueStorage storage;

	public MemoryGroupQueueNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType){
		super(params, clientType);
		codec = new MemoryGroupQueueCodec<>(getFieldInfo());
		storage = new MemoryGroupQueueStorage();
	}

	/*------------- QueueStorage --------------*/

	@Override
	public List<D> pollMulti(Config config){
		throw new UnsupportedOperationException();
	}

	/*------------- QueueStorageReader --------------*/

	@Override
	public GroupQueueMessage<PK,D> peek(Config config){
		long visibilityTimeoutMs = config.findVisibilityTimeoutMs().orElse(DEFAULT_VISIBILITY_TIMEOUT_MS);
		return Scanner.of(storage.peek(1, visibilityTimeoutMs))
				.map(codec::memoryMessageToGroupQueueMessage)
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<GroupQueueMessage<PK,D>> peekMulti(Config config){
		long visibilityTimeoutMs = config.findVisibilityTimeoutMs().orElse(DEFAULT_VISIBILITY_TIMEOUT_MS);
		var remainingLimit = new AtomicInteger(config.findLimit().orElseThrow());
		return Scanner.generate(() -> storage.peek(
						Math.min(remainingLimit.get(), MAX_MESSAGES_PER_PEEK),
						visibilityTimeoutMs))
				.advanceUntil(List::isEmpty)
				.each(batch -> remainingLimit.addAndGet(-batch.size()))
				.concat(Scanner::of)
				.map(codec::memoryMessageToGroupQueueMessage)
				.list();
	}

	@Override
	public Scanner<GroupQueueMessage<PK,D>> peekUntilEmpty(Config config){
		long visibilityTimeoutMs = config.findVisibilityTimeoutMs().orElse(DEFAULT_VISIBILITY_TIMEOUT_MS);
		return Scanner.generate(() -> storage.peek(MAX_MESSAGES_PER_PEEK, visibilityTimeoutMs))
				.advanceUntil(List::isEmpty)
				.concat(Scanner::of)
				.map(codec::memoryMessageToGroupQueueMessage);
	}

	/*------------- QueueStorageWriter --------------*/

	@Override
	public void ack(QueueMessageKey key, Config config){
		Optional.of(key)
				.map(QueueMessageKey::getHandle)
				.map(codec::bytesToId)
				.ifPresent(storage::ack);
	}

	@Override
	public void ackMulti(Collection<QueueMessageKey> keys, Config config){
		Scanner.of(keys)
				.map(QueueMessageKey::getHandle)
				.map(codec::bytesToId)
				.forEach(storage::ack);
	}

	/*------------- StorageWriter --------------*/

	@Override
	public void put(D databean, Config config){
		putMulti(List.of(databean), config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		Scanner.of(databeans)
				.batch(10)
				.map(codec::databeansToMemoryMessage)
				.forEach(storage::add);
	}

}
