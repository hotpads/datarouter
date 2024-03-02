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
package io.datarouter.client.memory.node.blobqueue;

import java.util.Map;
import java.util.Optional;

import io.datarouter.bytes.Codec;
import io.datarouter.client.memory.node.queue.MemoryQueueCodec;
import io.datarouter.client.memory.node.queue.MemoryQueueMessage;
import io.datarouter.client.memory.node.queue.MemoryQueueStorage;
import io.datarouter.model.databean.EmptyDatabean;
import io.datarouter.model.databean.EmptyDatabean.EmptyDatabeanFielder;
import io.datarouter.model.key.EmptyDatabeanKey;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.PhysicalBlobQueueStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.queue.BlobQueueMessage;
import io.datarouter.types.Ulid;

public class MemoryBlobQueueNode<T>
extends BasePhysicalNode<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder>
implements PhysicalBlobQueueStorageNode<T>{

	private final MemoryQueueStorage storage;
	private final Codec<T,byte[]> codec;

	public MemoryBlobQueueNode(
			NodeParams<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder> params,
			ClientType<?,?> clientType, Codec<T,byte[]> codec){
		super(params, clientType);
		storage = new MemoryQueueStorage();
		this.codec = codec;
	}

	@Override
	public int getMaxRawDataSize(){
		return Integer.MAX_VALUE;
	}

	@Override
	public Codec<T,byte[]> getCodec(){
		return codec;
	}

	@Override
	public void putRaw(byte[] data, Config config){
		storage.add(new MemoryQueueMessage(new Ulid().value(), data));
	}

	@Override
	public Optional<BlobQueueMessage<T>> peek(Config config){
		long visibilityTimeoutMs = config.findVisibilityTimeoutMs()
				.orElse(MemoryQueueStorage.DEFAULT_VISIBILITY_TIMEOUT_MS);
		var messages = storage.peek(1, visibilityTimeoutMs);
		if(messages.isEmpty()){
			return Optional.empty();
		}
		return memoryQueueMessageToOptionalBlobQueueMessageDto(messages.getFirst());
	}

	@Override
	public void ack(byte[] handle, Config config){
		storage.ack(MemoryQueueCodec.bytesToId(handle));
	}

	@Override
	public Optional<BlobQueueMessage<T>> poll(Config config){
		return memoryQueueMessageToOptionalBlobQueueMessageDto(storage.poll());
	}

	private Optional<BlobQueueMessage<T>> memoryQueueMessageToOptionalBlobQueueMessageDto(MemoryQueueMessage message){
		return Optional.of(new BlobQueueMessage<>(
				MemoryQueueCodec.idToBytes(message.getId()),
				message.getValue(),
				Map.of(),
				codec));
	}

}
