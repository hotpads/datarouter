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

import java.util.Map;
import java.util.Optional;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.filesystem.raw.queue.DirectoryQueue;
import io.datarouter.model.databean.EmptyDatabean;
import io.datarouter.model.databean.EmptyDatabean.EmptyDatabeanFielder;
import io.datarouter.model.key.EmptyDatabeanKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.PhysicalBlobQueueStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.queue.BlobQueueMessage;

public class DirectoryBlobQueueNode<T>
extends BasePhysicalNode<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder>
implements PhysicalBlobQueueStorageNode<T>{

	private final DirectoryQueue directoryQueue;
	private final Codec<T,byte[]> codec;

	public DirectoryBlobQueueNode(
			DirectoryQueue directoryQueue,
			NodeParams<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder> params,
			Codec<T,byte[]> codec){
		super(params, null);
		this.directoryQueue = directoryQueue;
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
		directoryQueue.putMessage(data);
	}

	@Override
	public Optional<BlobQueueMessage<T>> peek(Config config){
		return directoryQueue.peek()
				.map(message -> new BlobQueueMessage<>(message.getIdUtf8Bytes(), message.content, Map.of(), codec));
	}

	@Override
	public void ack(byte[] handle, Config config){
		directoryQueue.ack(StringCodec.UTF_8.decode(handle));
	}

}
